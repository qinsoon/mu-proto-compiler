package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;

public class InstRetVoid extends Instruction {
	
	public InstRetVoid() {
		this.opcode = OpCode.RETVOID;
	}

	@Override
	public String prettyPrint() {
		return "RETVOID";
	}

}
