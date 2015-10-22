package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;

public class InstThrow extends Instruction {
	Value exception;
	
	public InstThrow(Value exception) {
		super();
		this.exception = exception;
		this.operands.add(exception);
		
		this.opcode = OpCode.THROW;
	}
	
	public Value getExceptionObject() {
		return exception;
	}
	
	@Override
	public boolean needsToExpandIntoRuntimeCall() {
		return true;
	}
	
	@Override
	public String prettyPrint() {
		return "(THROW " + exception.prettyPrint() + ")";
	}

}
