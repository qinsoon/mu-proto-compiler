package parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiler.UVMCompiler;
import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncSigContext;
import parser.uIRParser.ImmediateContext;
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
import uvm.inst.InstRet2;
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
        if (ctx instanceof parser.uIRParser.InstParamContext) {
            Instruction node = new InstParam((int)getIntImmediateValue(((parser.uIRParser.InstParamContext) ctx).intImmediate()));
            String id = getIdentifierName(((parser.uIRParser.InstParamContext) ctx).IDENTIFIER(), false);
            node.setDefReg(f.findOrCreateRegister(id));
            return node;
        } else if (ctx instanceof parser.uIRParser.InstBranchContext) {
            String target = getIdentifierName(((parser.uIRParser.InstBranchContext) ctx).IDENTIFIER(), false);
            return new InstBranch(f.findOrCreateLabel(target));
        } else if (ctx instanceof parser.uIRParser.InstBranch2Context) {
            parser.uIRParser.ValueContext condContext = ((parser.uIRParser.InstBranch2Context) ctx).value();
            
            Value cond = getValue(f, condContext);            
            Label ifTrue = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) ctx).IDENTIFIER(0), false));
            Label ifFalse = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) ctx).IDENTIFIER(1), false));
            
            return new InstBranch2(cond, ifTrue, ifFalse);
        } 
        
        else if (ctx instanceof parser.uIRParser.InstPhiContext) {
            Type t = getType(((parser.uIRParser.InstPhiContext) ctx).type());
            
            String out = getIdentifierName(((parser.uIRParser.InstPhiContext) ctx).IDENTIFIER(0), false);
            
            Value val1 = getValue(f, ((parser.uIRParser.InstPhiContext) ctx).value(0));
            Label label1 = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstPhiContext) ctx).IDENTIFIER(1), false));
            Value val2 = getValue(f, ((parser.uIRParser.InstPhiContext) ctx).value(1));
            Label label2 = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstPhiContext) ctx).IDENTIFIER(2), false));
            
            Instruction node = new InstPhi(t, val1, label1, val2, label2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        
        else if (ctx instanceof parser.uIRParser.InstRet2Context) {
            Value v = getValue(f, ((parser.uIRParser.InstRet2Context) ctx).value());
            Instruction node = new InstRet2(v);            
            return node;
        }
        
        // bin op
        else if (ctx instanceof parser.uIRParser.InstShlContext) {
            Type t = getType(((parser.uIRParser.InstShlContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstShlContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstShlContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstShlContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstShl(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        else if (ctx instanceof parser.uIRParser.InstAddContext) {
            Type t = getType(((parser.uIRParser.InstAddContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstAddContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstAddContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstAddContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstAdd(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        else if (ctx instanceof parser.uIRParser.InstSremContext) {
            Type t = getType(((parser.uIRParser.InstSremContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstSremContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstSremContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstSremContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstSrem(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        else if (ctx instanceof parser.uIRParser.InstSgtContext) {
            Type t = getType(((parser.uIRParser.InstSgtContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstSgtContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstSgtContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstSgtContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstSgt(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        else if (ctx instanceof parser.uIRParser.InstSltContext) {
            Type t = getType(((parser.uIRParser.InstSltContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstSltContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstSltContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstSltContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstSlt(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
            return node;
        }
        else if (ctx instanceof parser.uIRParser.InstEqContext) {
            Type t = getType(((parser.uIRParser.InstEqContext) ctx).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstEqContext) ctx).value(0));
            Value op2 = getValue(f, ((parser.uIRParser.InstEqContext) ctx).value(1));
            
            String out = getIdentifierName(((parser.uIRParser.InstEqContext) ctx).IDENTIFIER(), false);
            
            Instruction node = new InstEq(t, op1, op2);
            node.setDefReg(f.findOrCreateRegister(out));
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
