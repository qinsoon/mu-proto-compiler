package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;
import uvm.type.IRef;

public class InstLoadInt extends AbstractLoad {

	
	public InstLoadInt(IRef type, Value addr) {
		super(type, addr);
				
		this.opcode = OpCode.LOADINT;
	}
	
	@Override
	public String prettyPrint() {
		return "(LOADINT " + addr.prettyPrint() + ")";
	}

}
