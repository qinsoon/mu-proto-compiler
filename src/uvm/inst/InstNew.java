package uvm.inst;

import java.util.List;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;

public class InstNew extends Instruction {
	Type type;
	
	public InstNew(Type type) {
		this.type = type;
		
		this.opcode = OpCode.NEW;
	}

	@Override
	public String prettyPrint() {
		return "(NEW " + type.prettyPrint() + ")";
	}

	public Type getType() {
		return type;
	}
	
	@Override
    public boolean needsToExpandIntoRuntimeCall() {
    	return true;
    }
}
