package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSub extends AbstractTypedBinop {

	public InstSub(Type type, Value op1, Value op2) {
		super(type, op1, op2);
		
		this.opcode = OpCode.SUB;
	}

	@Override
	public String prettyPrint() {
		return "(SUB " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
	}

}
