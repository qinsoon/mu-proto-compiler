package compiler.phase.mc.x64;

import java.util.List;
import java.util.ArrayList;

import burm.mc.X64add;
import burm.mc.X64mov;
import burm.mc.X64pop;
import burm.mc.X64push;
import burm.mc.X64store_mov;
import burm.mc.X64store_movsd;
import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.inst.AbstractCall;
import uvm.inst.InstCall;
import uvm.inst.InstPseudoCCInstruction;
import uvm.inst.InstTailCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCDPImmediate;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCLabeledMemoryOperand;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;

public class X64UVMCallConvention extends X64CDefaultCallConvention {
	int stackDispForLastThreadEntryFunction = 0;
	
	/**
	 * use this after call setupStackForThreadEntryFunction()
	 * @return how many bytes the stack top has been moved (negative number)
	 */
	public int getStackDispForLastThreadEntryFunction() {
		return stackDispForLastThreadEntryFunction;
	}
	
	public List<AbstractMachineCode> setupStackForThreadEntryFunction(CompiledFunction callerCF, Function calleeCF, List<uvm.Value> arguments, uvm.mc.MCDispMemoryOperand stackLoc) {
		List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
		int stackDisp = 0;
		
		int stackSizeForArguments = stackForArguments(calleeCF.getSig());
		if (stackSizeForArguments != 0) {
			UVMCompiler.unimplemented("setup thread entry function: some arguments are passed by stack");
			
			// not reached
			stackDisp -= stackSizeForArguments;
			stackLoc = stackLoc.cloneWithDisp(stackDisp);
		}
		
		// push entry function address
		stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
		stackLoc = stackLoc.cloneWithDisp(stackDisp);
		
		X64push pushEntry = new X64push();
		MCLabeledMemoryOperand entryLabel = new MCLabeledMemoryOperand(callerCF.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
		entryLabel.setDispLabel(new MCLabel(calleeCF.getName()));
		pushEntry.setOperand0(entryLabel);
		ret.add(pushEntry);
		
		List<String> paramRegs = pickRegistersForArguments(calleeCF.getSig());
		
		for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPRParam(); i++) {
			stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
			stackLoc = stackLoc.cloneWithDisp(stackDisp);
			
			String reg = UVMCompiler.MCDriver.getGPRParamName(i);
			
			int indexInParams = paramRegs.indexOf(reg);
			X64store_mov store = new X64store_mov();
			if (indexInParams == -1) {
				// this GPR is not used, store a zero into its stack slot
				store.setOperand0(stackLoc);
				store.setOperand1(MCIntImmediate.ZERO);
			} else {
				store.setOperand0(stackLoc);
				store.setOperand1(arguments.get(indexInParams).getMCOp());
			}
			
			store.setComment(reg);
			
			ret.add(store);
		}
		
		for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfFPRParam(); i++) {
			stackDisp -= UVMCompiler.MC_FP_REG_SIZE_IN_BYTES;
			stackLoc = stackLoc.cloneWithDisp(stackDisp);
			
			String reg = UVMCompiler.MCDriver.getFPRParamName(i);
			
			int indexInParams = paramRegs.indexOf(reg);
			X64store_mov store = new X64store_mov();
			if (indexInParams == -1) {
				// this FPR is not used, store a zero into its stack slot
				store.setOperand0(stackLoc);
				store.setOperand1(MCIntImmediate.ZERO);
			} else {
				store.setOperand0(stackLoc);
				store.setOperand1(callerCF.findOrCreateRegister(reg, MCRegister.MACHINE_REG, MCRegister.DATA_DP));
			}
			
			store.setComment(reg);
			
			ret.add(store);
		}
		
		stackDispForLastThreadEntryFunction = stackDisp;
		return ret;
	}
	
	@Override
    protected void pushFuncID(CompiledFunction cf, List<AbstractMachineCode> prologue) {
    	X64mov saveFuncID = new X64mov();
    	MCIntImmediate id = new MCIntImmediate(cf.getOriginFunction().getID());
    	MCDispMemoryOperand slot = new MCDispMemoryOperand(cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR), -8);
    	saveFuncID.setOperand0(id);
    	saveFuncID.setDefine(slot);
    	
    	prologue.add(saveFuncID);
    }
	
	@Override
	public List<AbstractMachineCode> callerSetupCallSequence(
            CompiledFunction caller, 
            AbstractCall call,
            AbstractMachineCode callMC) {
		if (! (call instanceof InstTailCall)) {
			return super.callerSetupCallSequence(caller, call, callMC);
		} else {
			MCRegister rsp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
			
			// tail call - see Tiger Book P319
			List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
			
			// handle parameters
			ret.addAll(handleArgumentsForCall(caller, call));
			
			// restore callee-saved regs
	       for (int i = caller.calleeSavedRegs.size() - 1; i >= 0; i--) {
	            ret.addAll(popStack(caller.calleeSavedRegs.get(i), rsp, InstPseudoCCInstruction.CALLEE_RESTORE_REGISTERS));
	        }
	       
	       // destroy frame by adding an invalid value - we will patch this soon (see postRegAllocPatching)
	       X64add destroyFrame = new X64add();
	       destroyFrame.setOperand0(rsp);
	       destroyFrame.setOperand1(new MCIntImmediate(1));
	       destroyFrame.setDefine(rsp);
	       ret.add(destroyFrame);
	       
	       // pop RBP
	       MCRegister rbp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
	       ret.addAll(super.popStack(rbp, rsp, null));
	       
	       // jump to callee (reserve the inst)
	       ret.add(callMC);
			
	       return ret;
		}
	}
	
	@Override
	public List<AbstractMachineCode> callerCleanupCallSequence(
            CompiledFunction caller, 
            AbstractCall call,
            AbstractMachineCode callMC) {
		if (! (call instanceof InstTailCall)) {
			return super.callerCleanupCallSequence(caller, call, callMC);
		} else {
			// do nothing
			return new ArrayList<AbstractMachineCode>();
		}
	}
	
	@Override
	public void postRegAllocPatching(CompiledFunction cf) {
		super.postRegAllocPatching(cf);
		
		for (MCBasicBlock bb : cf.BBs) {
			for (int i = 0; i < bb.getMC().size(); i++) {
				AbstractMachineCode mc = bb.getMC().get(i);
				
				if (mc.isTailCall()) {
					// patch the destroy frame inst
					X64add destroyFrame = (X64add) bb.getMC().get(i-2);
					destroyFrame.setOperand1(new MCIntImmediate(- cf.stackManager.getFinalStackDisp()));
				}
			}
		}
	}
}