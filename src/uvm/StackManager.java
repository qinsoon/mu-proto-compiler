package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import compiler.UVMCompiler;
import uvm.inst.AbstractCall;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;

public class StackManager {
	int stackSlot;
	int stackDisp;
	
	public int getStackSlot() {
		return stackSlot;
	}

	public int getStackDisp() {
		return stackDisp;
	}

	CompiledFunction current;
	
	HashMap<MCRegister, MCMemoryOperand> allocated = new HashMap<MCRegister, MCMemoryOperand>();
	
	List<FrameSlot> calleeSavedRegisters = new ArrayList<FrameSlot>();
	HashMap<AbstractCall, List<FrameSlot>> callerSavedRegisters = new HashMap<AbstractCall, List<FrameSlot>>();
	
	public StackManager(CompiledFunction cf) {
		this.current = cf;
		this.stackSlot = 0;
		this.stackDisp = 0;
	} 
	
	public void addCalleeSavedRegister(MCRegister r) {
		if (r.getDataType() == MCRegister.DATA_GPR) {
			stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
		} else if (r.getDataType() == MCRegister.DATA_DP) {
			stackDisp -= UVMCompiler.MC_FP_REG_SIZE_IN_BYTES;
		} else {
			UVMCompiler.error("unimplemented type: " + r.getDataType());
		}
		
		stackSlot ++;
		
		FrameSlot slot = new FrameSlot(stackDisp, stackSlot, r, r.getHighLevelOp());
		calleeSavedRegisters.add(slot);
	}
	
	public void addCallerSavedRegister(AbstractCall call, List<MCRegister> regs) {
		int tempStackDisp = stackDisp;
		int tempStackSlot = stackSlot;
		
		List<FrameSlot> slots = new ArrayList<FrameSlot>();
		
		for (MCRegister reg : regs) {
			if (reg.getDataType() == MCRegister.DATA_GPR) {
				tempStackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
			} else if (reg.getDataType() == MCRegister.DATA_DP) {
				tempStackDisp -= UVMCompiler.MC_FP_REG_SIZE_IN_BYTES;
			} else {
				UVMCompiler.error("unimplemented type: " + reg.getDataType());
			}
			
			tempStackSlot++;
			
			FrameSlot slot = new FrameSlot(tempStackDisp, tempStackSlot, reg, reg.getHighLevelOp());
			slots.add(slot);
		}
		
		callerSavedRegisters.put(call, slots);
	}
	
	/**
	 * returns the memory operand, caller should be responsible to set it as the spill
	 * @param spill
	 * @return
	 */
    public MCMemoryOperand spillInterval(Interval spill) {
    	// check if we have allocated stack slot for this virtual reg
    	MCMemoryOperand old = allocated.get(spill.getOrig());
    	if (old != null)
    		return old;
    	
    	MCDispMemoryOperand mem = new MCDispMemoryOperand();
    	mem.setBase(current.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
    	if (spill.getDataType() == MCRegister.DATA_GPR) {
    		// set current mem op
    		mem.setDisp(stackDisp);
    		mem.setSize((byte) 8);
    		
    		// move to next slot
    		stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
    		stackSlot ++;
    	} else if (spill.getDataType() == MCRegister.DATA_DP || spill.getDataType() == MCRegister.DATA_SP) {
    		UVMCompiler.error("spilling floating point register to memory, unimplemented");
    	} else {
    		UVMCompiler.error("spilling unknown register type to memory");
    	}
    	
    	allocated.put(spill.getOrig(), mem);
    	
    	return mem;
    }
}
