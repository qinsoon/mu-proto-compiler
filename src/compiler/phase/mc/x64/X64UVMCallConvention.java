package compiler.phase.mc.x64;

import java.util.List;
import java.util.ArrayList;

import burm.mc.X64add;
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
import uvm.inst.InstCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCDPImmediate;
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
}