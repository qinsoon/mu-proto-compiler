package uvm.inst;

import uvm.Instruction;

public class InstThreadExit extends Instruction {

	@Override
    public boolean needsToExpandIntoRuntimeCall() {
    	return true;
    }
	
	@Override
	public String prettyPrint() {
		return "THREADEXIT";
	}

}
