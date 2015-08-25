package compiler.phase.mc.x64;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import burm.mc.*;
import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.inst.AbstractCall;
import uvm.inst.InstCCall;
import uvm.inst.InstCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCLabeledMemoryOperand;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;

public class X64CDefaultCallConvention {
	/**
	 * a list of register names, mapped to each parameter
	 * if a parameter is passed on stack, it appears as null in the list
	 * @param sig
	 * @return
	 */
	public static List<String> pickRegistersForArguments(FunctionSignature sig) {
		List<String> regs = new ArrayList<String>();
		
        int usedParamGPRs = 0;
        int usedParamFPRs = 0;
        
        List<uvm.Type> argTypes = sig.getParamTypes();
        
        for (int i = 0; i < argTypes.size(); i++) {
            Type curType = argTypes.get(i);
            if (curType.fitsInGPR() > 0) {
                if (curType.fitsInGPR() == 1) {                	
                    if (usedParamGPRs < UVMCompiler.MCDriver.getNumberOfGPRParam()) {
                        // pass by register
                    	regs.add(UVMCompiler.MCDriver.getGPRParamName(usedParamGPRs));
                        usedParamGPRs++;
                    } else {
                        // pass by stack
                    	regs.add(null);
                    }
                } else {
                    UVMCompiler.error("argument requires more than one GPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() > 0){
                if (curType.fitsInFPR() == 1) {
                    if (usedParamFPRs < UVMCompiler.MCDriver.getNumberOfFPRParam()) {
                        // pass by register
                    	regs.add(UVMCompiler.MCDriver.getFPRParamName(usedParamFPRs));                        
                        usedParamFPRs++;
                    } else {
                        // pass by stack
                    	regs.add(null);
                    }
                } else {
                    UVMCompiler.error("argument requires more than one FPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() == 0 && curType.fitsInGPR() == 0) {
                UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + curType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
        
        return regs;
	}
	
	// TODO: 
	public static int stackForArguments(FunctionSignature sig) {
		return 0;
	}
	
    public void calleeInitParameterRegisters(CompiledFunction cf) {
        // set param_regi to the ith of param registers if suitable
        
        int usedGPRParamReg = 0;
        int usedFPRParamReg = 0;
        
        List<Type> paramTypes = cf.getOriginFunction().getSig().getParamTypes();
        for (int i = 0; 
                i < paramTypes.size() && 
                (usedGPRParamReg < UVMCompiler.MCDriver.getNumberOfGPRParam() || usedFPRParamReg < UVMCompiler.MCDriver.getNumberOfFPRParam());
                i++) {
            Type paramType = paramTypes.get(i);
            System.out.println("allocate param register for PARAM" + i);
            if (paramType.fitsInGPR() > 0) {
                if (paramTypes.get(i).fitsInGPR() == 1) {
                    // we need to set param_regi to the ith of GPR Param
                    MCRegister symbolParamReg = cf.findRegister("param_reg"+i, MCRegister.PARAM_REG);
                    MCRegister realParamReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRParamName(usedGPRParamReg), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                    usedGPRParamReg++;
                    
                    cf.usedParamRegs.add(realParamReg);
                    
                    symbolParamReg.setREP(realParamReg);
                } else {
                    // fits in several GPRs
                    UVMCompiler.error("a param fits in several GPRs, unimplemented");
                }
            } else if (paramType.fitsInFPR() > 0) {
                if (paramTypes.get(i).fitsInFPR() == 1) {
                    // double precision
                    MCRegister symbolParamReg = cf.findRegister("param_reg"+i, MCRegister.PARAM_REG);
                    MCRegister realParamReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFPRParamName(usedFPRParamReg), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                    usedFPRParamReg++;
                    
                    cf.usedParamRegs.add(realParamReg);
                    
                    symbolParamReg.setREP(realParamReg);
                } else {
                    UVMCompiler.error("a param fits in single-precision FPRs, unimplemented");                    
                }
            } else if (paramType.fitsInGPR() == 0 && paramType.fitsInFPR() == 0) {
            	if (paramType instanceof uvm.type.Void) {
            		// do nothing
            	} else            		
            		UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + paramType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
    }
    
    public void calleeInitReturnRegister(CompiledFunction cf) {
        // set ret_regi to the ith of return registers
        
        Type returnType = cf.getOriginFunction().getSig().getReturnType();
        
        if (returnType.fitsInGPR() > 0) {
            if (returnType.fitsInGPR() == 1) {
                // set ret_reg0 to the 0th of return register
                MCRegister symbolRetReg = cf.findRegister("ret_reg0", MCRegister.RET_REG);
                MCRegister realRetReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                
                symbolRetReg.setREP(realRetReg);
            } else {
                UVMCompiler.error("a return value fits in several GPRs, unimplemented");
            }
        } else if (returnType.fitsInFPR() > 0) {
            if (returnType.fitsInFPR() == 1) {
                MCRegister symbolRetReg = cf.findRegister("ret_reg0", MCRegister.RET_REG);
                MCRegister realRetReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                
                symbolRetReg.setREP(realRetReg);
            } else {
                UVMCompiler.error("a return value fits in single-precision FPRs, unimplemented");
            }
        } else if (returnType.fitsInGPR() == 0 && returnType.fitsInFPR() == 0) {
            if (returnType instanceof uvm.type.Void) {
                // we dont need to do anything
            } else { 
                UVMCompiler.error("a return value of Type " + returnType.prettyPrint() + " doesnt fit in registers, and passed on stack. unimplemented");
            }
        } else {
            UVMCompiler.error("Type " + returnType.prettyPrint() + " seems errornous on its fitness of registers. ");
        }
    }
    
    int argumentsSizeOnStack = -1;
    List<MCRegister> callerSavedRegs = new ArrayList<MCRegister>();     // push order
    
    public List<AbstractMachineCode> callerSetupCallSequence(
            CompiledFunction caller, 
            AbstractCall call,
            AbstractMachineCode callMC) {
        List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
        
        MCRegister rsp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);        
        
        // caller saved registers
        callerSavedRegs.clear();
        int callMCIndex = callMC.sequence;
        System.out.println("setup call seq at " + callMCIndex);
        caller.printInterval();
        List<MCRegister> liveRegs = caller.getLiveRegistersThrough(callMCIndex);		// we only need to save scratch registers that are alive through the call inst
//        MCRegister.printList("liveThroughs", liveRegs);
        // add live-in regs
        MCBasicBlock callBB = caller.getBasicBlockFor(callMC);
        for (MCRegister reg : callBB.liveIn)
            if (!liveRegs.contains(reg.REP()))
                liveRegs.add(reg.REP());
//        MCRegister.printList("liveThrough+live-in", liveRegs);

        for (MCRegister reg : liveRegs) {
            System.out.println("Pushing " + reg.prettyPrint() + ", hash:" + reg.hashCode());
            ret.addAll(pushStack(reg, rsp));
            callerSavedRegs.add(reg);
        }
        
        // deal with arguments
        List<Value> args = call.getArguments();
        List<Type> argTypes = call.getSig().getParamTypes();
        
        UVMCompiler._assert(
                argTypes.size() == args.size(), 
                "call IR has " + args.size() + " arguments while func signature has " + argTypes.size());
        
        int usedParamGPRs = 0;
        int usedParamFPRs = 0;
        argumentsSizeOnStack = 0;
        
        for (int i = 0; i < argTypes.size(); i++) {
            Type curType = argTypes.get(i);
            MCOperand arg = operandFromNode(caller, args.get(i));
            if (curType.fitsInGPR() > 0) {
                if (curType.fitsInGPR() == 1) {
                	
                	// if we want to pass label as an argument, convert it to a memory op
                	MCLabeledMemoryOperand argAsMemOp = null;
                	if (arg instanceof MCLabel) {
                		// use label(%rip)
                		argAsMemOp = new MCLabeledMemoryOperand();
                		argAsMemOp.setBase(caller.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
                		argAsMemOp.setDispLabel((MCLabel) arg);
                		argAsMemOp.setSize((byte)8);
                	}
                	
                    if (usedParamGPRs < UVMCompiler.MCDriver.getNumberOfGPRParam()) {
                        // pass by register
                        MCRegister nextAvailParamReg = caller.findOrCreateRegister(
                                UVMCompiler.MCDriver.getGPRParamName(usedParamGPRs),
                                MCRegister.MACHINE_REG,
                                MCRegister.DATA_GPR);
                        
                        if (argAsMemOp == null) {
                        	// normal mov (from reg to reg)
                        	ret.add(UVMCompiler.MCDriver.genMove(nextAvailParamReg, arg));
                        }
                        else {
                        	// lea
                        	X64lea lea = new X64lea();
                        	lea.setOperand(0, argAsMemOp);
                        	lea.setDefine(nextAvailParamReg);
                        	
                        	ret.add(lea);
                        }
                        
                        usedParamGPRs++;
                    } else {
                        // pass by stack
                        ret.addAll(pushStack(arg, rsp));
                        argumentsSizeOnStack += UVMCompiler.MC_REG_SIZE_IN_BYTES;
                    }
                } else {
                    UVMCompiler.error("argument requires more than one GPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() > 0){
                if (curType.fitsInFPR() == 1) {
                    if (usedParamFPRs < UVMCompiler.MCDriver.getNumberOfFPRParam()) {
                        // pass by register
                        MCRegister nextAvailParamReg = caller.findOrCreateRegister(
                                UVMCompiler.MCDriver.getFPRParamName(usedParamFPRs), 
                                MCRegister.MACHINE_REG, 
                                MCRegister.DATA_DP);
                        
                        ret.add(UVMCompiler.MCDriver.genDPMove(nextAvailParamReg, arg));
                        
                        usedParamFPRs++;
                    } else {
                        // pass by stack
                        ret.addAll(pushStack(arg, rsp));
                        argumentsSizeOnStack += UVMCompiler.MC_FP_REG_SIZE_IN_BYTES;
                    }
                } else {
                    UVMCompiler.error("argument requires more than one FPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() == 0 && curType.fitsInGPR() == 0) {
                UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + curType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
        
        // push return address - dont need to do this
//        MCLabel callerLabel = (MCLabel) operandFromNode(caller, caller.getOriginFunction().getFuncLabel());
//        MCMemoryOperand callerRelAddress = new MCMemoryOperand();
//        MCRegister rip = caller.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
//        callerRelAddress.setBase(rip);
//        callerRelAddress.setDispLabel(callerLabel);
//        ret.add(pushStack(callerRelAddress));
        
        // generate call
        // FIXME: why generate a new call inst instead of using the old one?
        ret.add(callMC);
//        ret.add(UVMCompiler.MCDriver.genCall((MCLabel) operandFromNode(caller, callee.getFuncLabel())));
        
        return ret;
    }
    
    public List<AbstractMachineCode> callerCleanupCallSequence(
            CompiledFunction caller, 
            AbstractCall call,
            AbstractMachineCode callMC) {
        List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
        
        // trash arguments on stack
        // add rsp, xxx
        if (argumentsSizeOnStack == -1)
            UVMCompiler.error("unknown arguments size on stack: should call callerSetupCallSequence() before call callerCleanupCallSequence()");
        
        if (argumentsSizeOnStack != 0) {
            MCRegister rsp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
            X64add trashArguments = new X64add();
            trashArguments.setOperand0(rsp);
            trashArguments.setOperand1(new MCIntImmediate(argumentsSizeOnStack));
            trashArguments.setDefine(rsp);
            ret.add(trashArguments);
        }
        
        // save return value to designated reg or memory location
        MCRegister retResult = callMC.getDefineAsReg().REP();
        
        Type returnType = call.getSig().getReturnType();
        
        if (returnType.fitsInGPR() > 0) {
            if (returnType.fitsInGPR() == 1) {
                MCRegister realRetReg = caller.findOrCreateRegister(UVMCompiler.MCDriver.getGPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                ret.add(UVMCompiler.MCDriver.genMove(retResult, realRetReg));
            } else {
                UVMCompiler.error("a return value fits in several GPRs, unimplemented");
            }
        } else if (returnType.fitsInFPR() > 0) {
            if (returnType.fitsInFPR() == 1) {
                MCRegister realRetReg = caller.findOrCreateRegister(UVMCompiler.MCDriver.getFPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                
                ret.add(UVMCompiler.MCDriver.genDPMove(retResult, realRetReg));
            } else {
                UVMCompiler.error("a return value fits in single-precision FPRs, unimplemented");
            }
        } else if (returnType.fitsInGPR() == 0 && returnType.fitsInFPR() == 0) {
            if (returnType instanceof uvm.type.Void) {
                // we dont need to do anything
            } else { 
                UVMCompiler.error("a return value of Type " + returnType.prettyPrint() + " doesnt fit in registers, and passed on stack. unimplemented");
            }
        } else {
            UVMCompiler.error("Type " + returnType.prettyPrint() + " seems errornous on its fitness of registers. ");
        }
        
        // restore caller saved register
        MCRegister rsp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        for (int i = callerSavedRegs.size() - 1; i >= 0; i--) {
            ret.addAll(popStack(callerSavedRegs.get(i), rsp));
        }
        
        return ret;
    }
    
    public void genPrologue(CompiledFunction cf) {
        List<AbstractMachineCode> prologue = cf.prologue;
        
        // set up its own frame
        
        // push rbp
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        MCRegister rsp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        
        List<X64MachineCode> pushRBP = pushStack(rbp, rsp);
        pushRBP.get(0).setLabel(new MCLabel("prologue"));
        prologue.addAll(pushRBP);
        
        // rsp -> rbp
        prologue.add(UVMCompiler.MCDriver.genMove(rbp, rsp));
        
        // allocate space for local storage
        int stackDisp = -1;

        if (stackDisp != 0) {
            X64add dispRSP = new X64add();
            dispRSP.setOperand0(rsp);
            dispRSP.setOperand1(new MCIntImmediate(stackDisp));
            dispRSP.setDefine(rsp);
            prologue.add(dispRSP);
        }
        
        // callee-saved register
        if (cf.getOriginFunction().isMain()) {
        	// push all general-purpose calleeSavedRegs
        	for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
        		String r = UVMCompiler.MCDriver.getGPRName(i);
        		if (UVMCompiler.MCDriver.isCalleeSave(r)) {
        			MCRegister reg = cf.findOrCreateRegister(r, MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        			cf.calleeSavedRegs.add(reg);
        			prologue.addAll(pushStack(reg, rsp));
        		}
        	}
        } else {
	        for (MCRegister reg : cf.intervals.keySet()) {
	        	// FIXME: this is not correct
	            Interval li = cf.intervals.get(reg);
	            if (UVMCompiler.MCDriver.isCalleeSave(reg.getName()) && li.hasValidRange()) {
	                cf.calleeSavedRegs.add(reg);
	                // need to save it
	                prologue.addAll(pushStack(reg, rsp));
	            }
	        }
        }
    }
    
    public static void postRegAllocPatching(CompiledFunction cf) {
    	int stackDisp = cf.stackManager.getStackDisp();
    	
    	UVMCompiler._assert(stackDisp <= 0, "expecting a stack displacement to be zero or negative");
    	
    	int absDisp = Math.abs(stackDisp);
    	if (absDisp % 16 != 0) {    		
    		absDisp = (absDisp / 16 + 1) * 16;
    		stackDisp = - absDisp;
    	}
    	if (stackDisp == 0) {
    		stackDisp = -16;
    	}
    	
    	X64add dispRSP = (X64add) cf.prologue.get(2);
    	dispRSP.setOperand(1, new MCIntImmediate(stackDisp));
    }
    
    public void genEpilogue(CompiledFunction cf) {
        List<AbstractMachineCode> epilogue = cf.epilogue;
        
        MCRegister rsp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        
        // restore callee-saved regs
        for (int i = cf.calleeSavedRegs.size() - 1; i >= 0; i--) {
            epilogue.addAll(popStack(cf.calleeSavedRegs.get(i), rsp));
        }
        
        // set rbp -> rsp
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        epilogue.add(UVMCompiler.MCDriver.genMove(rsp, rbp));
        
        // pop rbp
        epilogue.addAll(popStack(rbp, rsp));
    }
    
    private List<X64MachineCode> popStack(MCOperand dst, MCRegister rsp) {
    	if (!(dst instanceof MCRegister))
    		UVMCompiler.error("expecting pushing a mcregister here");
    	
    	MCRegister reg = (MCRegister) dst;
    	
    	if (reg.getDataType() == MCRegister.DATA_GPR)
    		return popStackInt(dst);
    	else return popStackFP(dst, rsp);
    }
    
    private List<X64MachineCode> pushStack(MCOperand src, MCRegister rsp) {
    	if (src instanceof MCRegister && ((MCRegister) src).getDataType() == MCRegister.DATA_GPR)
    		return pushStackInt(src);
    	else if (src instanceof MCIntImmediate)
    		return pushStackInt(src);
    	else return pushStackFP(src, rsp);
    }
    
    // FIXME: for floating-point register, need to movsd, then add rsp 8
    private List<X64MachineCode> popStackInt(MCOperand dst) {
        X64pop ret = new X64pop();
        ret.setOperand(0, dst);
        return Arrays.asList(ret);
    }
    
    private List<X64MachineCode> pushStackInt(MCOperand src) {
        X64push ret = new X64push();
        ret.setOperand(0, src);
        return Arrays.asList(ret);
    }
    
    private List<X64MachineCode> popStackFP(MCOperand dst, MCRegister rsp) {
    	MCDispMemoryOperand stackSlot = new MCDispMemoryOperand(rsp);
    	X64movsd mov = new X64movsd();
    	mov.setOperand0(stackSlot);
    	mov.setDefine(dst);
    	
    	X64add add = new X64add();
    	add.setOperand0(rsp);
    	add.setOperand1(new MCIntImmediate(UVMCompiler.MC_FP_REG_SIZE_IN_BYTES));
    	add.setDefine(rsp);
    	
    	List<X64MachineCode> ret = new ArrayList<X64MachineCode>();
    	ret.add(mov);
    	ret.add(add);
    	return ret;
    }
    
    private List<X64MachineCode> pushStackFP(MCOperand src, MCRegister rsp) {
    	X64sub sub = new X64sub();
    	sub.setOperand0(rsp);
    	sub.setOperand1(new MCIntImmediate(UVMCompiler.MC_FP_REG_SIZE_IN_BYTES));
    	sub.setDefine(rsp);
    	
    	MCDispMemoryOperand stackSlot = new MCDispMemoryOperand(rsp);
    	X64store_movsd mov = new X64store_movsd();
    	mov.setOperand0(stackSlot);
    	mov.setOperand1(src);
    	
    	List<X64MachineCode> ret = new ArrayList<X64MachineCode>();
    	ret.add(sub);
    	ret.add(mov);
    	return ret;
    }
    
    private static MCOperand operandFromNode(CompiledFunction cf, IRTreeNode node) {
        MCOperand ret;
        switch(node.getOpcode()) {
        case OpCode.INT_IMM:
          ret = new uvm.mc.MCIntImmediate(((uvm.IntImmediate)node).getValue()); break;
        case OpCode.FP_SP_IMM:
          ret = new uvm.mc.MCSPImmediate(((uvm.FPImmediate)node).getFloat()); break;
        case OpCode.FP_DP_IMM:
          ret = new uvm.mc.MCDPImmediate(((uvm.FPImmediate)node).getDouble()); break;
        case OpCode.REG_I1:
        case OpCode.REG_I8:
        case OpCode.REG_I16:
        case OpCode.REG_I32:
        case OpCode.REG_I64:
          ret = cf.findOrCreateRegister(
                  ((uvm.Register)node).getName(), 
                  uvm.mc.MCRegister.OTHER_SYMBOL_REG, 
                  uvm.mc.MCRegister.DATA_GPR); break;
        case OpCode.LABEL:
          ret = new uvm.mc.MCLabel(((uvm.Label)node).getName()); break;
        default:
          ret = cf.findOrCreateRegister(
                  "res_reg"+node.getId(), 
                  uvm.mc.MCRegister.RES_REG, 
                  getDataTypeFromHighLevelIR(node)); break;
        }
        ret.highLevelOp = node;
        return ret;
    }
    
    private static int getDataTypeFromHighLevelIR(IRTreeNode ir) {
        switch(ir.getOpcode()) {
        case OpCode.REG_I1:
        case OpCode.REG_I8:
        case OpCode.REG_I16:
        case OpCode.REG_I32:
        case OpCode.REG_I64:
        case OpCode.INT_IMM:
            return MCRegister.DATA_GPR;
        case OpCode.REG_SP:
        case OpCode.FP_SP_IMM:
            return MCRegister.DATA_SP;
        case OpCode.REG_DP:
        case OpCode.FP_DP_IMM:
            return MCRegister.DATA_DP;
        default:
            if (ir instanceof Instruction) {
                Instruction inst = (Instruction) ir;
                return getDataTypeFromHighLevelIR(inst.getDefReg());
            }
            UVMCompiler.error("cannot get data type from HLL IR:" + ir.prettyPrint());
            return -1;
        }
    }
}