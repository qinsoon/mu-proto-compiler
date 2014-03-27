package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiler.UVMCompiler;
import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncSigContext;
import parser.uIRParser.ImmediateContext;
import parser.uIRParser.InstBodyContext;
import parser.uIRParser.InstContext;
import parser.uIRParser.IntImmediateContext;
import parser.uIRParser.TypeContext;
import parser.uIRParser.TypeDescriptorContext;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.Register;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstAdd;
import uvm.inst.InstBranch;
import uvm.inst.InstBranch2;
import uvm.inst.InstEq;
import uvm.inst.InstParam;
import uvm.inst.InstPhi;
import uvm.inst.InstRet;
import uvm.inst.InstSgt;
import uvm.inst.InstShl;
import uvm.inst.InstSlt;
import uvm.inst.InstSrem;
import uvm.metadata.Const;
import uvm.type.Int;

public abstract class ASTHelper {
    private ASTHelper() {}
    
    public static String getIdentifierName(TerminalNode id, boolean expectGlobal) 
            throws ASTParsingException {
        String text = id.getText();
        
        if (expectGlobal && text.charAt(0) != '@')
            throw new ASTParsingException("Met identifier" + id.getText() + 
                    " while expecting a global identifier");
        
        if (text.charAt(0) == '@' || text.charAt(0) == '%')
            return text.substring(1);

        throw new ASTParsingException("Invalid identifier " + id.getText() + 
                ", expecting @ or % prefix");
    }
    
    public static FunctionSignature getFunctionSignature(FuncSigContext ctx) throws ASTParsingException{
        List<TypeContext> types = ctx.type();
        
        Type returnType = null;
        List<Type> paramTypes = new ArrayList<Type>();
        
        for (int i = 0; i < types.size(); i++) {
            if (i == 0) {
                // return type
                returnType = getType(types.get(i));
            } else  
                // param type 
                paramTypes.add(getType(types.get(i)));
        }
        
        return new FunctionSignature(returnType, paramTypes);
    }

    public static Type getType(TypeContext typeContext) throws ASTParsingException {
        if (typeContext.typeDescriptor() != null) {
            // defining a type via type descriptor
            TypeDescriptorContext ctx = typeContext.typeDescriptor();
            if (ctx instanceof parser.uIRParser.IntTypeContext) {
                int size = Integer.parseInt(
                        ((parser.uIRParser.IntTypeContext) ctx).intImmediate().getText());
                return Int.findOrCreate(size);
            } else {
                throw new ASTParsingException("Missing implementation on " + ctx.getClass().toString());
            }
        } else {
            // referring a type via IDENTIFIER
            throw new ASTParsingException("Missing implementation on type by IDENTIFIER");
        }
    }
    
    public static Const getConst(ConstDefContext ctx) throws ASTParsingException {
        String name = getIdentifierName(ctx.IDENTIFIER(), false);
        Type type = getType(ctx.type());
        Number imm = getImmediateValue(ctx.immediate(), type);
        
        return new Const(name, type, imm);
    }
    
    public static Number getImmediateValue(ImmediateContext ctx, Type t) throws ASTParsingException {
        Number ret;
        if (t instanceof uvm.type.Int) {
            ret = Long.parseLong(ctx.getText());
        } else if (t instanceof uvm.type.Float) {
            ret = Float.parseFloat(ctx.getText());
        } else if (t instanceof uvm.type.Double) {
            ret = Double.parseDouble(ctx.getText());
        } else throw new ASTParsingException("Invalid immediate type: " + t);
        
        return ret;
    }
    
    public static Instruction getInstruction(Function f, InstContext ctx) throws ASTParsingException {
        Register def = null;
        if (ctx.IDENTIFIER() != null)
            def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false));
        
        InstBodyContext inst = ctx.instBody();
        
        if (inst instanceof parser.uIRParser.InstParamContext) {
            Instruction node = new InstParam((int)getIntImmediateValue(((parser.uIRParser.InstParamContext) inst).intImmediate()));
            node.setDefReg(def);
            return node;
        } else if (inst instanceof parser.uIRParser.InstBranchContext) {
            String target = getIdentifierName(((parser.uIRParser.InstBranchContext) inst).IDENTIFIER(), false);
            return new InstBranch(f.findOrCreateLabel(target));
        } else if (inst instanceof parser.uIRParser.InstBranch2Context) {
            parser.uIRParser.ValueContext condContext = ((parser.uIRParser.InstBranch2Context) inst).value();
            
            Value cond = getValue(f, condContext);            
            Label ifTrue = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) inst).IDENTIFIER(0), false));
            Label ifFalse = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) inst).IDENTIFIER(1), false));
            
            return new InstBranch2(cond, ifTrue, ifFalse);
        } else if (inst instanceof parser.uIRParser.InstPhiContext) {
            Type t = getType(((parser.uIRParser.InstPhiContext) inst).type());
            
            HashMap<Label, Value> values = new HashMap<Label, Value>();
            for (int i = 0; i < ((parser.uIRParser.InstPhiContext) inst).IDENTIFIER().size(); i++) {
                Value v = getValue(f, ((parser.uIRParser.InstPhiContext) inst).value(i));
                Label l = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstPhiContext) inst).IDENTIFIER(i), false));
                values.put(l, v);
            }
            
            Instruction node = new InstPhi(t, values);
            
            node.setDefReg(def);
            return node;
        } else if (inst instanceof parser.uIRParser.InstRetContext) {
            Value v = getValue(f, ((parser.uIRParser.InstRetContext) inst).value());
            Instruction node = new InstRet(v);
            return node;
        } 
        // bin op
        else if (inst instanceof parser.uIRParser.InstBinOpContext) {
            Type t = getType(((parser.uIRParser.InstBinOpContext) inst).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstBinOpContext) inst).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstBinOpContext) inst).value(1));
            
            parser.uIRParser.BinOpsContext binOp = ((parser.uIRParser.InstBinOpContext) inst).binOps();

            Instruction node = null;
            if (binOp.iBinOps() != null) {
                parser.uIRParser.IBinOpsContext iBinOp = binOp.iBinOps();
                if (iBinOp instanceof parser.uIRParser.InstShlContext) {
                    node = new InstShl(t, op1, op2);
                } else if (iBinOp instanceof parser.uIRParser.InstAddContext) {
                    node = new InstAdd(t, op1, op2);
                } else if (iBinOp instanceof parser.uIRParser.InstSRemContext) {
                    node = new InstSrem(t, op1, op2);
                } else {
                    UVMCompiler.error("incomplete implementation of i binops: " + inst.getText());
                }
            } else if (binOp.fBinOps() != null) {
                UVMCompiler.error("havent implemented f binops");
            }
            
            node.setDefReg(def);
            return node;
        }
        // cmp
        else if (inst instanceof parser.uIRParser.InstCmpContext) {
            Type t = getType(((parser.uIRParser.InstCmpContext) inst).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstCmpContext) inst).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstCmpContext) inst).value(1));
            
            parser.uIRParser.CmpOpsContext cmpOp = ((parser.uIRParser.InstCmpContext) inst).cmpOps();
            
            Instruction node = null;
            if (cmpOp.iCmpOps() != null) {
                parser.uIRParser.ICmpOpsContext iCmpOp = cmpOp.iCmpOps();
                if (iCmpOp instanceof parser.uIRParser.InstEqContext) {
                    node = new InstEq(t, op1, op2);
                } else if (iCmpOp instanceof parser.uIRParser.InstSgtContext) {
                    node = new InstSgt(t, op1, op2);
                } else if (iCmpOp instanceof parser.uIRParser.InstSltContext) {
                    node = new InstSlt(t, op1, op2);
                } else {
                    UVMCompiler.error("incomplete implementation of i cmp op: " + inst.getText());
                }
            } else if (cmpOp.fCmpOps() != null) {
                UVMCompiler.error("havent implemented f cmp ops");
            }
            
            node.setDefReg(def);
            return node;
        }
        
        else {
            UVMCompiler.error("incomplete implementation of " + ctx.getClass().toString());
        }
        return null;
    }
    
    private static Value getValue(Function f, parser.uIRParser.ValueContext ctx) throws ASTParsingException {
        Value ret = null;
        
        if (ctx.immediate() != null) {
            if (ctx.immediate().intImmediate() != null)
                ret = new IntImmediate(getIntImmediateValue(ctx.immediate().intImmediate()));
            else {
                UVMCompiler.error("Missing implementation for fp immediate");
            }
        } else {
            String id = getIdentifierName(ctx.IDENTIFIER(), false);
            ret = f.findOrCreateRegister(id);
        }
        
        return ret;
    }

    private static long getIntImmediateValue(IntImmediateContext intImmediate) {
        return Long.parseLong(intImmediate.getText());
    }
}
