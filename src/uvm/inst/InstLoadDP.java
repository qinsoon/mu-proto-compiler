package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;
import uvm.type.IRef;

public class InstLoadDP extends AbstractLoad {

	public InstLoadDP(IRef type, Value addr) {
		super(type, addr);
		
		this.opcode = OpCode.LOADDP;
	}
	
	@Override
	public String prettyPrint() {
		return "(LOADDP " + addr.prettyPrint() + ")";
	}

}
