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
import uvm.MicroVM;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.inst.AbstractCall;
import uvm.inst.InstCCall;
import uvm.inst.InstCall;
import uvm.inst.InstPseudoCCInstruction;
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
        for (MCRegister reg : liveRegs) {
        	System.out.println(reg.prettyPrint());
        }
//        UVMCompiler._suspend("check");
        
        // add live-in regs
        MCBasicBlock callBB = caller.getBasicBlockFor(callMC);
        for (MCRegister reg : callBB.liveIn) {
        	MCRegister mcreg = caller.intervals.get(reg.REP()).getPhysicalReg();
            if (!liveRegs.contains(mcreg) && mcreg != null) {
            	System.out.println(mcreg.prettyPrint());
                liveRegs.add(mcreg);
            }
        }
//        UVMCompiler._suspend("added live in");
        
        // check if we are calling into UVM functions
        if (MicroVM.v.getFunction(call.getFunc()) != null) {
        	// if we are calling into C, we save everything here (otherwise a weird bug may happen at C side)
        	// if we are calling into UVM, we will need to remove callee saved regs
        	
        	List<MCRegister> callerSaved = new ArrayList<MCRegister>();
        	
        	for (MCRegister reg : liveRegs)
        		if (!UVMCompiler.MCDriver.isCalleeSave(reg.getName()))
        			callerSaved.add(reg);
        	
        	liveRegs = callerSaved;
        }
        
        for (MCRegister reg : liveRegs) {
            System.out.println("Pushing " + reg.prettyPrint() + ", hash:" + reg.hashCode());
            ret.addAll(pushStack(reg, rsp, InstPseudoCCInstruction.CALLER_SAVE_REGISTERS));
            callerSavedRegs.add(reg);
            caller.stackManager.addCallerSavedRegister(call, liveRegs);
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

//            System.out.println("calling " + call.prettyPrint());
//            System.out.println(" with arg " + arg.prettyPrint());
            
            // get machine register for arg
            MCOperand mcArg = null;
            if (arg instanceof MCRegister) {
            	mcArg = caller.intervals.get(((MCRegister) arg).REP()).getPhysicalReg();
            	if (mcArg == null)
            		mcArg = caller.intervals.get(((MCRegister) arg).REP()).getPhysicalLocation();
//            	System.out.println("      mcarg " + mcArg.prettyPrint());
            }
            if (mcArg != null)
            	arg = mcArg;
            
//            UVMCompiler._suspend(arg.prettyPrint());
            
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
                        	AbstractMachineCode mov = UVMCompiler.MCDriver.genMove(nextAvailParamReg, arg);
                        	mov.setHighLevelIR(InstPseudoCCInstruction.CALLER_PREPARE_PARAM_REG);
                        	ret.add(mov);
                        }
                        else {
                        	// lea
                        	X64lea lea = new X64lea();
                        	lea.setHighLevelIR(InstPseudoCCInstruction.CALLER_PREPARE_PARAM_REG);
                        	lea.setOperand(0, argAsMemOp);
                        	lea.setDefine(nextAvailParamReg);
                        	
                        	ret.add(lea);
                        }
                        
                        usedParamGPRs++;
                    } else {
                        // pass by stack
                    	ret.addAll(pushStack(arg, rsp, InstPseudoCCInstruction.CALLER_PREPARE_PARAM_REG));
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
                        
                        AbstractMachineCode dpmov = UVMCompiler.MCDriver.genDPMove(nextAvailParamReg, arg);
                        dpmov.setHighLevelIR(InstPseudoCCInstruction.CALLER_PREPARE_PARAM_REG);
                        ret.add(dpmov);
                        
                        usedParamFPRs++;
                    } else {
                        // pass by stack
                    	ret.addAll(pushStack(arg, rsp, InstPseudoCCInstruction.CALLER_PREPARE_PARAM_REG));
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
        
        // generate call
        ret.add(callMC);
        
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
            ret.addAll(popStack(callerSavedRegs.get(i), rsp, InstPseudoCCInstruction.CALLER_RESTORE_REGISTERS));
        }
        
        return ret;
    }
    
    public void genPrologue(CompiledFunction cf) {
        List<AbstractMachineCode> prologue = cf.prologue;
        
        // set up its own frame
        
        // push rbp - 1st inst (0)
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        MCRegister rsp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        
        List<X64MachineCode> pushRBP = pushStack(rbp, rsp, null);
        pushRBP.get(0).setLabel(new MCLabel("prologue"));
        prologue.addAll(pushRBP);
        
        // rsp -> rbp - 2nd inst (1)
        prologue.add(UVMCompiler.MCDriver.genMove(rbp, rsp));
        
        // allocate space for local storage - 3rd inst(2)
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
        	// save all general-purpose calleeSavedRegs
        	for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
        		String r = UVMCompiler.MCDriver.getGPRName(i);
        		if (UVMCompiler.MCDriver.isCalleeSave(r)) {
        			MCRegister reg = cf.findOrCreateRegister(r, MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        			cf.calleeSavedRegs.add(reg);
        			cf.stackManager.addCalleeSavedRegister(reg);
        			prologue.addAll(pushStack(reg, rsp, InstPseudoCCInstruction.CALLEE_SAVE_REGISTERS));
        		}
        	}
        } else {
	        for (MCRegister reg : cf.intervals.keySet()) {     	
	            Interval li = cf.intervals.get(reg);
	            MCRegister mcreg = li.getPhysicalReg();
	            if (UVMCompiler.MCDriver.isCalleeSave(mcreg.getName()) && li.hasValidRange()) {
	            	if (cf.calleeSavedRegs.contains(mcreg))
	            		continue;	            		
	            	
	                // need to save it
	            	cf.calleeSavedRegs.add(mcreg);
	            	cf.stackManager.addCalleeSavedRegister(reg);
	                prologue.addAll(pushStack(mcreg, rsp, InstPseudoCCInstruction.CALLEE_SAVE_REGISTERS));
	            }
	        }
//	        for (MCRegister reg : cf.calleeSavedRegs) {
//	        	System.out.println(reg.prettyPrint());
//	        }
//	        UVMCompiler._suspend("callee saved register for " + cf.getOriginFunction().getName());
        }
    }
    
    public void postRegAllocPatching(CompiledFunction cf) {    	   	
    	// patch the stack pointer change
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
            epilogue.addAll(popStack(cf.calleeSavedRegs.get(i), rsp, InstPseudoCCInstruction.CALLEE_RESTORE_REGISTERS));
        }
        
        // set rbp -> rsp
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        epilogue.add(UVMCompiler.MCDriver.genMove(rsp, rbp));
        
        // pop rbp
        epilogue.addAll(popStack(rbp, rsp, null));
    }
    
    private List<X64MachineCode> popStack(MCOperand dst, MCRegister rsp, InstPseudoCCInstruction hll) {
    	if (!(dst instanceof MCRegister))
    		UVMCompiler.error("expecting pushing a mcregister here");
    	
    	MCRegister reg = (MCRegister) dst;
    	
    	if (reg.getDataType() == MCRegister.DATA_GPR)
    		return popStackInt(dst, hll);
    	else return popStackFP(dst, rsp, hll);
    }
    
    private List<X64MachineCode> pushStack(MCOperand src, MCRegister rsp, InstPseudoCCInstruction hll) {
    	if (src instanceof MCRegister && ((MCRegister) src).getDataType() == MCRegister.DATA_GPR)
    		return pushStackInt(src, hll);
    	else if (src instanceof MCIntImmediate)
    		return pushStackInt(src, hll);
    	else return pushStackFP(src, rsp, hll);
    }
    
    private List<X64MachineCode> popStackInt(MCOperand dst, InstPseudoCCInstruction hll) {
        X64pop ret = new X64pop();
        ret.setOperand(0, dst);
        ret.setHighLevelIR(hll);
        return Arrays.asList(ret);
    }
    
    private List<X64MachineCode> pushStackInt(MCOperand src, InstPseudoCCInstruction hll) {
        X64push ret = new X64push();
        ret.setOperand(0, src);
        ret.setHighLevelIR(hll);
        return Arrays.asList(ret);
    }
    
    private List<X64MachineCode> popStackFP(MCOperand dst, MCRegister rsp, InstPseudoCCInstruction hll) {
    	MCDispMemoryOperand stackSlot = new MCDispMemoryOperand(rsp);
    	X64movsd mov = new X64movsd();
    	mov.setOperand0(stackSlot);
    	mov.setDefine(dst);
    	mov.setHighLevelIR(hll);
    	
    	X64add add = new X64add();
    	add.setOperand0(rsp);
    	add.setOperand1(new MCIntImmediate(UVMCompiler.MC_FP_REG_SIZE_IN_BYTES));
    	add.setDefine(rsp);
    	add.setHighLevelIR(hll);
    	
    	List<X64MachineCode> ret = new ArrayList<X64MachineCode>();
    	ret.add(mov);
    	ret.add(add);
    	return ret;
    }
    
    private List<X64MachineCode> pushStackFP(MCOperand src, MCRegister rsp, InstPseudoCCInstruction hll) {
    	X64sub sub = new X64sub();
    	sub.setOperand0(rsp);
    	sub.setOperand1(new MCIntImmediate(UVMCompiler.MC_FP_REG_SIZE_IN_BYTES));
    	sub.setDefine(rsp);
    	sub.setHighLevelIR(hll);
    	
    	MCDispMemoryOperand stackSlot = new MCDispMemoryOperand(rsp);
    	X64store_movsd mov = new X64store_movsd();
    	mov.setOperand0(stackSlot);
    	mov.setOperand1(src);
    	mov.setHighLevelIR(hll);
    	
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