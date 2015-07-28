package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;

public class InstNewThread extends Instruction {
	uvm.Register stack;
	
	public InstNewThread(uvm.Register stack) {
		this.stack = stack;
		
		this.operands.add(stack);
		this.opcode = OpCode.NEWTHREAD;
	}
	
	public uvm.Register getStack() {
		return stack;
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
