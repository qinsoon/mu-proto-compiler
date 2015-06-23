package uvm.inst;

import uvm.OpCode;
import uvm.Value;

public class InstInternalIRefOffset extends AbstractInternalInstruction {
	Value loc;
	Value offset;
	
	public InstInternalIRefOffset(Value loc, Value offset) {
		this.loc = loc;
		this.offset = offset;
		
		this.opcode = OpCode.IREF_OFFSET;
		
		this.operands.add(loc);
		this.operands.add(offset);
	}
	
	public Value getLoc() {
		return loc;
	}
	
	public Value getOffset() {
		return offset;
	}

	@Override
	public String prettyPrint() {
		return "(IREF_OFFSET " + loc.prettyPrint() + " " + offset.prettyPrint() + ")";
	}

}
