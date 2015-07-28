package uvm.inst;

import uvm.Instruction;
import uvm.IntImmediate;
import uvm.OpCode;
import uvm.Value;
import uvm.type.Array;

public class InstGetElemIRefConstIndex extends Instruction {
	Array arrayType;
	Value loc;
	long index;
	
	public InstGetElemIRefConstIndex(Array arrayType, long index, Value loc) {
		this.arrayType = arrayType;
		this.index = index;
		this.loc = loc;
		
		this.operands.add(loc);
		this.opcode = OpCode.GETELEM_CONST;
	}
	
	public Array getArrayType() {
		return arrayType;
	}
	
	public Value getLoc() {
		return loc;
	}
	
	public long getIndex() {
		return index;
	}

	@Override
	public String prettyPrint() {
		return "(GETELEM_CONST " + arrayType.prettyPrint() + " " + index + " " + loc.prettyPrint() + ")";
	}
}
