package uvm.inst;

import uvm.Instruction;
import uvm.MicroVM;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.type.Struct;

public class InstGetFieldIRef extends Instruction {
	Struct structType;
	int index;
	Value loc;
	
	int offset;
	
	public InstGetFieldIRef(Struct structType, int index, Value loc) {
		this.structType = structType;
		this.index = index;
		this.loc = loc;
		
		this.operands.add(loc);
		this.opcode = OpCode.GETFIELD;
	}

	public Struct getStructType() {
		return structType;
	}

	public int getIndex() {
		return index;
	}

	public Value getLoc() {
		return loc;
	}

	@Override
	public String prettyPrint() {
		return "(GETFIRLEDIREF " + structType.prettyPrint() + " " + index + " " + loc.prettyPrint() + ")";
	}

}
