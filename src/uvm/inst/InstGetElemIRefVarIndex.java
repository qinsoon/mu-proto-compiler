package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;
import uvm.type.Array;

public class InstGetElemIRefVarIndex extends Instruction {
	Array arrayType;
	Value loc;
	Value index;
	
	public InstGetElemIRefVarIndex(Array arrayType, Value index, Value loc) {
		this.arrayType = arrayType;
		this.index = index;
		this.loc = loc;
		
		this.operands.add(loc);
		this.operands.add(index);
		this.opcode = OpCode.GETELEM_VAR;
	}	

	public Array getArrayType() {
		return arrayType;
	}
	
	public Value getLoc() {
		return loc;
	}

	public Value getIndex() {
		return index;
	}

	@Override
	public String prettyPrint() {
		return "(GETELEM_VAR " + arrayType.prettyPrint() + " " + index.prettyPrint() + " " + loc.prettyPrint() + ")";
	}
}
