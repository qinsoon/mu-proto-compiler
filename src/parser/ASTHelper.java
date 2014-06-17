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
import parser.uIRParser.TypeConstructorContext;
import parser.uIRParser.TypeContext;
import uvm.FPImmediate;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.MicroVM;
import uvm.Register;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstAdd;
import uvm.inst.InstBranch;
import uvm.inst.InstBranch2;
import uvm.inst.InstCall;
import uvm.inst.InstEq;
import uvm.inst.InstFAdd;
import uvm.inst.InstFDiv;
import uvm.inst.InstFOlt;
import uvm.inst.InstFPToSI;
import uvm.inst.InstParam;
import uvm.inst.InstPhi;
import uvm.inst.InstRet;
import uvm.inst.InstSIToFP;
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
        if (typeContext.typeConstructor() != null) {
            // defining a type via type descriptor
            TypeConstructorContext ctx = typeContext.typeConstructor();
            if (ctx instanceof parser.uIRParser.IntTypeContext) {
                int size = Integer.parseInt(
                        ((parser.uIRParser.IntTypeContext) ctx).intImmediate().getText());
                return Int.findOrCreate(size);
            } else if (ctx instanceof parser.uIRParser.DoubleTypeContext) {
                return uvm.type.Double.DOUBLE;
            }            
            else {
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
//        Register def = null;
//        if (ctx.IDENTIFIER() != null)
//            def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false));
        
        InstBodyContext inst = ctx.instBody();
        
        /*
         * pseudo inst
         */
        if (inst instanceof parser.uIRParser.InstParamContext) {
            int paramIndex = (int)getIntImmediateValue(((parser.uIRParser.InstParamContext) inst).intImmediate());
            Instruction node = new InstParam(paramIndex);
            Type t = f.getSig().getParamTypes().get(paramIndex);
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), t);
            node.setDefReg(def);
            return node;
        } 
        
        /*
         * branch inst
         */
        else if (inst instanceof parser.uIRParser.InstBranchContext) {
            String target = getIdentifierName(((parser.uIRParser.InstBranchContext) inst).IDENTIFIER(), false);
            return new InstBranch(f.findOrCreateLabel(target));
        } 
        else if (inst instanceof parser.uIRParser.InstBranch2Context) {
            parser.uIRParser.ValueContext condContext = ((parser.uIRParser.InstBranch2Context) inst).value();
            
            Type int1Type = uvm.type.Int.findOrCreate(1);
            Value cond = getValue(f, condContext, int1Type);            
            Label ifTrue = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) inst).IDENTIFIER(0), false));
            Label ifFalse = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstBranch2Context) inst).IDENTIFIER(1), false));
            
            return new InstBranch2(cond, ifTrue, ifFalse);
        } 
        else if (inst instanceof parser.uIRParser.InstRetContext) {
            Type t = getType(((parser.uIRParser.InstRetContext) inst).type());
            Value v = getValue(f, ((parser.uIRParser.InstRetContext) inst).value(), t);
            Instruction node = new InstRet(v);
            return node;
        } 
        
        else if (inst instanceof parser.uIRParser.InstPhiContext) {
            Type t = getType(((parser.uIRParser.InstPhiContext) inst).type());
            
            HashMap<Label, Value> values = new HashMap<Label, Value>();
            for (int i = 0; i < ((parser.uIRParser.InstPhiContext) inst).IDENTIFIER().size(); i++) {
                Value v = getValue(f, ((parser.uIRParser.InstPhiContext) inst).value(i), t);
                Label l = f.findOrCreateLabel(getIdentifierName(((parser.uIRParser.InstPhiContext) inst).IDENTIFIER(i), false));
                values.put(l, v);
            }
            
            Instruction node = new InstPhi(t, values);
            
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), t);
            node.setDefReg(def);
            return node;
        } 
        /*
         * bin op
         */
        else if (inst instanceof parser.uIRParser.InstBinOpContext) {
            Type t = getType(((parser.uIRParser.InstBinOpContext) inst).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstBinOpContext) inst).value(0), t);
            Value op2 = getValue(f, ((parser.uIRParser.InstBinOpContext) inst).value(1), t);
            
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
                parser.uIRParser.FBinOpsContext fBinOp = binOp.fBinOps();
                if (fBinOp instanceof parser.uIRParser.InstFAddContext) {
                    node = new InstFAdd(t, op1, op2);
                } else if (fBinOp instanceof parser.uIRParser.InstFDivContext) {
                    node = new InstFDiv(t, op1, op2);
                } else {
                    UVMCompiler.error("incomplete implementation of f binops: " + inst.getText());
                }
            }
            
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), t);
            node.setDefReg(def);
            return node;
        }
        /*
         * comparison
         */
        else if (inst instanceof parser.uIRParser.InstCmpContext) {
            Type t = getType(((parser.uIRParser.InstCmpContext) inst).type());
            Value op1 = getValue(f, ((parser.uIRParser.InstCmpContext) inst).value(0), t);
            Value op2 = getValue(f, ((parser.uIRParser.InstCmpContext) inst).value(1), t);
            
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
                parser.uIRParser.FCmpOpsContext fCmpOp = cmpOp.fCmpOps();
                if (fCmpOp instanceof parser.uIRParser.InstFOltContext) {
                    node = new InstFOlt(t, op1, op2);
                } else {
                    UVMCompiler.error("incomplete implementation of f cmp op: " + inst.getText());
                }
            }
            
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), t);
            node.setDefReg(def);
            return node;
        }
        /*
         * conversion
         */
        else if (inst instanceof parser.uIRParser.InstConversionContext) {
            parser.uIRParser.InstConversionContext convCtx = (parser.uIRParser.InstConversionContext) inst;
            Type fromType = getType(convCtx.type(0));
            Type toType = getType(convCtx.type(1));
            Value op = getValue(f, convCtx.value(), fromType);
            
            parser.uIRParser.ConvOpsContext convOp = convCtx.convOps();
            
            Instruction node = null;
            if (convOp instanceof parser.uIRParser.InstSIToFPContext) {
                node = new InstSIToFP(fromType, toType, op);
            } else if (convOp instanceof parser.uIRParser.InstFPToSIContext) {
                node = new InstFPToSI(fromType, toType, op);
            } else {
                UVMCompiler.error("incomplete implementation of conversion inst: " + convOp.getClass().getName());
            }
            
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), toType);
            node.setDefReg(def);
            return node;
        } else if (inst instanceof parser.uIRParser.InstCallContext) {
            parser.uIRParser.InstCallContext callCtx = (parser.uIRParser.InstCallContext) inst;
            
            parser.uIRParser.FuncCallBodyContext calleeCtx = callCtx.funcCallBody();
            String calleeName = getIdentifierName(calleeCtx.value().IDENTIFIER(), true);
            Function callee = MicroVM.v.getFunction(calleeName);
            FunctionSignature sig = getFunctionSignature(calleeCtx.funcSig());
            if (callee == null) {
                callee = new Function(calleeName, sig);
                MicroVM.v.declareFunc(calleeName, callee);
            }
            
            parser.uIRParser.ArgsContext argsCtx = calleeCtx.args();
            List<uvm.Value> args = new ArrayList<uvm.Value>();
            for (int i = 0; i < argsCtx.value().size(); i++) {
                parser.uIRParser.ValueContext valueCtx = argsCtx.value(i);
                args.add(getValue(f, valueCtx, sig.getParamTypes().get(i)));
            }
            
            Instruction node = new InstCall(callee, args);
            
            Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), sig.getReturnType());
            node.setDefReg(def);
            return node;
        }
        
        else {
            UVMCompiler.error("incomplete implementation of " + ctx.getClass().toString());
        }
        return null;
    }
    
    private static Value getValue(Function f, parser.uIRParser.ValueContext ctx, uvm.Type type) throws ASTParsingException {
        Value ret = null;
        
        if (ctx.immediate() != null) {
            if (ctx.immediate().intImmediate() != null) {
                ret = new IntImmediate(type, getIntImmediateValue(ctx.immediate().intImmediate()));
            }
            else if (ctx.immediate().fpImmediate() != null) {
                ret = new FPImmediate(type, getFPImmediateValue(ctx.immediate().fpImmediate()));
            }
            else {
                UVMCompiler.error("Missing implementation on immediate values (shouldn't happen, we implemented fp/int both)");
            }
        } else {
            String id = getIdentifierName(ctx.IDENTIFIER(), false);
            ret = f.findOrCreateRegister(id, type);
        }
        
        return ret;
    }

    private static long getIntImmediateValue(IntImmediateContext intImmediate) {
        return Long.parseLong(intImmediate.getText());
    }
    
    private static double getFPImmediateValue(parser.uIRParser.FpImmediateContext fpImmediate) {
        return Double.parseDouble(fpImmediate.getText());
    }
}
