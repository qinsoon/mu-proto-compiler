package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;
import uvm.type.IRef;

public class InstLoad extends Instruction {
	Value addr;
	IRef type;
	
	public InstLoad(IRef type, Value addr) {
		this.type = type;
		this.addr = addr;
		
		operands.add(addr);
				
		this.opcode = OpCode.LOAD;
	}
	
	@Override
	public String prettyPrint() {
		return "(LOAD " + addr.prettyPrint() + ")";
	}

}
