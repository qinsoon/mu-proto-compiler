package compiler.phase.mc.linearscan;

import java.util.HashMap;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCMemoryOperand;
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
	
	HashMap<MCRegister, MCMemoryOperand> allocated;
	
	public StackManager(CompiledFunction cf) {
		this.current = cf;
		this.stackSlot = 0;
		this.stackDisp = - UVMCompiler.MC_REG_SIZE_IN_BYTES;
		this.allocated = new HashMap<MCRegister, MCMemoryOperand>();
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
