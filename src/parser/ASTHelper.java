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
import uvm.ImmediateValue;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.MicroVM;
import uvm.Register;
import uvm.Type;
import uvm.Value;
import uvm.inst.*;
import uvm.metadata.Const;
import uvm.type.Array;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Struct;

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
    
    public static Type defineType(String id, TypeContext typeContext) throws ASTParsingException {
    	// handle possible self-reference here    	
        TypeConstructorContext ctx = typeContext.typeConstructor();
        if (ctx instanceof parser.uIRParser.StructTypeContext) {
        	// create a dummy first
        	Struct dummy = Struct.createDummyStruct();
        	MicroVM.v.declareType(id, dummy);
        	
        	// fill in
        	ArrayList<Type> types = new ArrayList<Type>();
        	for (TypeContext typeCtx : ((parser.uIRParser.StructTypeContext) ctx).type()) {
        		types.add(getType(typeCtx));
        	}
        	
        	dummy.setTypes(types);
        	
        	return dummy;
        }
        
        Type t = getType(typeContext);
        MicroVM.v.declareType(id, t);
        return t;
    }

    public static Type getType(TypeContext typeContext) throws ASTParsingException {
        if (typeContext.typeConstructor() != null) {
            // defining a type via type descriptor
            TypeConstructorContext ctx = typeContext.typeConstructor();
            // int
            if (ctx instanceof parser.uIRParser.IntTypeContext) {
                int size = Integer.parseInt(
                        ((parser.uIRParser.IntTypeContext) ctx).intImmediate().getText());
                return Int.findOrCreate(size);
            } 
            // double
            else if (ctx instanceof parser.uIRParser.DoubleTypeContext) {
                return uvm.type.Double.DOUBLE;
            }        
            // ref
            else if (ctx instanceof parser.uIRParser.RefTypeContext) {
            	return Ref.findOrCreateRef(getType(((parser.uIRParser.RefTypeContext) ctx).type()));
            }
            // iref
            else if (ctx instanceof parser.uIRParser.IRefTypeContext) {
            	return IRef.findOrCreateIRef(getType(((parser.uIRParser.IRefTypeContext) ctx).type()));
            }
            // struct
            else if (ctx instanceof parser.uIRParser.StructTypeContext) {
            	ArrayList<Type> types = new ArrayList<Type>();
            	for (TypeContext typeCtx : ((parser.uIRParser.StructTypeContext) ctx).type()) {
            		types.add(getType(typeCtx));
            	}
            	return Struct.findOrCreateStruct(types);
            }
            // opaque types
            else if (ctx instanceof parser.uIRParser.StackTypeContext) {
            	return uvm.type.Stack.T;
            }
            else if (ctx instanceof parser.uIRParser.ThreadTypeContext) {
            	return uvm.type.Thread.T;
            }
            // void
            else if (ctx instanceof parser.uIRParser.VoidTypeContext) {
            	return uvm.type.Void.T;
            }
            else if (ctx instanceof parser.uIRParser.ArrayTypeContext) {
            	uvm.Type eleT = getType(((parser.uIRParser.ArrayTypeContext) ctx).type());
            	int length = Integer.valueOf(((parser.uIRParser.ArrayTypeContext) ctx).intImmediate().getText());
            	return uvm.type.Array.findOrCreate(eleT, length);
            }
            else {
                throw new ASTParsingException("Missing implementation on " + ctx.getClass().toString());
            }
        } else {
            // referring a type via IDENTIFIER
        	String id = getIdentifierName(typeContext.IDENTIFIER(), false);
        	Type t = MicroVM.v.types.get(id);
        	if (t == null)
        		throw new ASTParsingException("Retrieving a type that is not defined before: " + id);
        	return t;
        }
    }
    
    public static ImmediateValue getConst(ConstDefContext ctx) throws ASTParsingException {
        Type type = getType(ctx.type());
        
        if (type instanceof uvm.type.Int) {
        	return new IntImmediate(type, Long.parseLong(ctx.immediate().getText()));
        } else if (type instanceof uvm.type.Float || type instanceof uvm.type.Double) {
        	return new FPImmediate(type, Double.parseDouble(ctx.immediate().getText()));
        } else {
        	UVMCompiler.error("unexpected const type: " + type.prettyPrint());
        	return null;
        }
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
        InstBodyContext inst = ctx.instBody();
        
        /*
         * pseudo inst
         */
        if (inst instanceof parser.uIRParser.InstParamContext) {
            int paramIndex = (int)getIntImmediateValue(((parser.uIRParser.InstParamContext) inst).intImmediate());
            Type t = f.getSig().getParamTypes().get(paramIndex);
            Instruction node = new InstParam(paramIndex, t);
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
        else if (inst instanceof parser.uIRParser.InstRetVoidContext) {
        	Instruction node = new InstRetVoid();
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
        } 
        /*
         * memory allocation
         */
        else if (inst instanceof parser.uIRParser.InstAllocaContext) {
        	parser.uIRParser.InstAllocaContext allocaCtx = (parser.uIRParser.InstAllocaContext) inst;
        	
        	Type t = getType(allocaCtx.type());
        	IRef irefT = IRef.findOrCreateIRef(t);
        	
        	Instruction node = new InstAlloca(t);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), irefT);
        	node.setDefReg(def);
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstNewContext) {
        	parser.uIRParser.InstNewContext newCtx = (parser.uIRParser.InstNewContext) inst;
        	
        	Type t = getType(newCtx.type());
        	Ref refT = Ref.findOrCreateRef(t);
        	
        	Instruction node = new InstNew(t);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), refT);
        	node.setDefReg(def);
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstNewStackContext) {
        	parser.uIRParser.InstNewStackContext newStackCtx = (parser.uIRParser.InstNewStackContext) inst;
        	
        	Function entryFunc = getFunction(newStackCtx.funcCallBody());
        	
        	List<Value> args = getArguments(f, newStackCtx.funcCallBody().args(), entryFunc.getSig());
        	
        	Instruction node = new InstNewStack(entryFunc, args);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), uvm.type.Stack.T);
        	node.setDefReg(def);
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstNewThreadContext) {
        	parser.uIRParser.InstNewThreadContext newThreadCtx = (parser.uIRParser.InstNewThreadContext) inst;
        	
        	Value stack = getValue(f, newThreadCtx.value(), uvm.type.Stack.T);
        	
        	Instruction node = new InstNewThread((uvm.Register)stack);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), uvm.type.Thread.T);
        	node.setDefReg(def);
        	return node;
        }
        /*
         * memory access
         */
        else if (inst instanceof parser.uIRParser.InstLoadContext) {
        	parser.uIRParser.InstLoadContext loadCtx = (parser.uIRParser.InstLoadContext) inst;
        	
        	Type referentType = getType(loadCtx.type());
        	IRef irefType = IRef.findOrCreateIRef(referentType);
        	
        	Value loc = getValue(f, loadCtx.value(), irefType);
        	
        	Instruction node = new InstLoad(irefType, loc);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), referentType);
        	node.setDefReg(def);
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstStoreContext) {
        	parser.uIRParser.InstStoreContext storeCtx = (parser.uIRParser.InstStoreContext) inst;
        	
        	Type referentType = getType(storeCtx.type());
        	IRef irefType = IRef.findOrCreateIRef(referentType);
        	
        	Value loc = getValue(f, storeCtx.value(0), irefType);
        	Value value = getValue(f, storeCtx.value(1), referentType);
        	
        	Instruction node = new InstStore(irefType, loc, value);
        	
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstGetFieldIRefContext) {
        	parser.uIRParser.InstGetFieldIRefContext getFieldCtx = (parser.uIRParser.InstGetFieldIRefContext) inst;

        	int index = (int) getIntImmediateValue(getFieldCtx.intImmediate());
        	
        	Struct structType = (Struct) getType(getFieldCtx.type());
        	IRef irefType = IRef.findOrCreateIRef(structType);
        	Type resType  = structType.getType(index);
        	
        	Value location = getValue(f, getFieldCtx.value(), irefType);
        	
        	Instruction node = new InstGetFieldIRef(structType, index, location);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), resType);
        	node.setDefReg(def);
        	
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstGetIRefContext) {
        	parser.uIRParser.InstGetIRefContext getIRefCtx = (parser.uIRParser.InstGetIRefContext) inst;
        	
        	Type referentT = getType(getIRefCtx.type());
        	Ref refT = Ref.findOrCreateRef(referentT);
        	IRef irefT = IRef.findOrCreateIRef(referentT);
        	
        	Value ref = getValue(f, getIRefCtx.value(), refT);
        	
        	Instruction node = new InstGetIRef(referentT, ref);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), irefT);
        	node.setDefReg(def);
        	
        	return node;
        }
        else if (inst instanceof parser.uIRParser.InstGetElemIRefContext) {
        	parser.uIRParser.InstGetElemIRefContext getElemIRefCtx = (parser.uIRParser.InstGetElemIRefContext) inst;
        	
        	Array arrayT = (Array) getType(getElemIRefCtx.type(0));
        	IRef arrayIRef = IRef.findOrCreateIRef(arrayT);
        	IRef arrayEleTypeIRef = IRef.findOrCreateIRef(arrayT.getEleType());        	
        	
        	Value loc = getValue(f, getElemIRefCtx.value(0), arrayIRef);
        	Type indexT = getType(getElemIRefCtx.type(1));
        	Value index = getValue(f, getElemIRefCtx.value(1), indexT);
        	
        	Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), arrayEleTypeIRef);
        	
        	if (index instanceof IntImmediate) {
        		Instruction node = new InstGetElemIRefConstIndex(arrayT, ((IntImmediate) index).getValue(), loc);
        		node.setDefReg(def);
        		return node;
        	} else {
        		Instruction node = new InstGetElemIRefVarIndex(arrayT, index, loc);
        		node.setDefReg(def);
        		return node;
        	}
        }
        /*
         * call
         */
        else if (inst instanceof parser.uIRParser.InstCallContext) {
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
        else if (inst instanceof parser.uIRParser.InstCCallContext) {
        	parser.uIRParser.InstCCallContext ccallCtx = (parser.uIRParser.InstCCallContext) inst;
        	
        	parser.uIRParser.FuncCallBodyContext calleeCtx = ccallCtx.funcCallBody();
        	
        	// call conv
        	int cc = -1;
        	parser.uIRParser.CallConvContext ccCtx = ccallCtx.callConv();
        	if (ccCtx instanceof parser.uIRParser.CCALL_DEFAULT_CCContext)
        		cc = InstCCall.CC_DEFAULT;
        	
        	// func name
        	String cFuncName = getIdentifierName(calleeCtx.value().IDENTIFIER(), true);
        	
        	// signature
        	FunctionSignature sig = getFunctionSignature(calleeCtx.funcSig());
        	
        	parser.uIRParser.ArgsContext argsCtx = calleeCtx.args();
        	List<uvm.Value> args = new ArrayList<uvm.Value>();
        	for (int i = 0; i < argsCtx.value().size(); i++) {
        		parser.uIRParser.ValueContext valueCtx = argsCtx.value(i);
        		args.add(getValue(f, valueCtx, sig.getParamTypes().get(i)));
        	}
        	
        	Instruction node = new InstCCall(cc, sig, cFuncName, args);
        	
        	if (ctx.IDENTIFIER() != null) {
        		Register def = f.findOrCreateRegister(getIdentifierName(ctx.IDENTIFIER(), false), sig.getReturnType());
        		node.setDefReg(def);
        	}
        	return node;
        }
        /*
         * Thread related
         */
        else if (inst instanceof parser.uIRParser.InstThreadExitContext) {
        	parser.uIRParser.InstThreadExitContext threadExitCtx = (parser.uIRParser.InstThreadExitContext) inst;
        	
        	Instruction node = new InstThreadExit();
        	return node;
        }
        /*
         * debug use
         */
        else if (inst instanceof parser.uIRParser.InstPrintStrContext) {
        	parser.uIRParser.InstPrintStrContext printStrCtx = (parser.uIRParser.InstPrintStrContext) inst;
        	
        	String literal = printStrCtx.STRINGLITERAL().toString();
        	if (literal.startsWith("\""))
        		literal = literal.substring(1);
        	if (literal.endsWith("\""))
        		literal = literal.substring(0, literal.length() - 1);
        	
        	Instruction node = new InstInternalPrintStr(literal);
        	return node;
        }
        
        else {
            UVMCompiler.error("incomplete implementation of " + ctx.toStringTree());
        }
        return null;
    }
    
    private static Function getFunction(parser.uIRParser.FuncCallBodyContext ctx) throws ASTParsingException {
    	String name = getIdentifierName(ctx.value().IDENTIFIER(), true);
    	Function f = MicroVM.v.getFunction(name);
    	FunctionSignature sig = getFunctionSignature(ctx.funcSig());
    	if (f == null) {
    		f = new Function(name, sig);
    		MicroVM.v.declareFunc(name, f);
    	}
    	return f;
    }
    
    private static List<Value> getArguments(Function f, parser.uIRParser.ArgsContext ctx, FunctionSignature sig) throws ASTParsingException {
    	List<Value> ret = new ArrayList<Value>();
    	for (int i = 0; i < ctx.value().size(); i++) {
    		parser.uIRParser.ValueContext valueCtx = ctx.value(i);
    		ret.add(getValue(f, valueCtx, sig.getParamTypes().get(i)));
    	}
    	return ret;
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
