package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;

public class InstAlloca extends Instruction {
	Type type;
	
	public InstAlloca(Type type) {
		this.type = type;
		
		this.opcode = OpCode.ALLOCA;
	}

	@Override
	public String prettyPrint() {
		return "(ALLOCA " + type.prettyPrint() + ")";
	}

	public Type getType() {
		return type;
	}

}
