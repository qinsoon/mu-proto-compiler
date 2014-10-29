package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;
import uvm.type.IRef;

public class InstStore extends Instruction {
	IRef addrType;
	Value addr;
	Value value;
	
	public InstStore(IRef addrType, Value addr, Value value) {
		this.addrType = addrType;
		this.addr = addr;
		this.value = value;
		
		operands.add(addr);
		operands.add(value);
		
		this.opcode = OpCode.STORE;
	}

	@Override
	public String prettyPrint() {
		return "(STORE " + addr.prettyPrint() + "," + value.prettyPrint() + ")";
	}

}
