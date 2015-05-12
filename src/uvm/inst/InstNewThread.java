package uvm.inst;

import uvm.Instruction;

public class InstNewThread extends Instruction {
	uvm.Value stack;
	
	public InstNewThread(uvm.Value stack) {
		this.stack = stack;
	}

	@Override
	public String prettyPrint() {
		return "NEWTHREAD(" + stack.prettyPrint() + ")";
	}

	@Override
    public boolean needsToExpandIntoRuntimeCall() {
    	return true;
    }
}
