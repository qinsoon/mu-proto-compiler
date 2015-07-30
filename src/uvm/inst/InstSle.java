package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSle extends AbstractTypedBinop {

	public InstSle(Type type, Value op1, Value op2) {
		super(type, op1, op2);
		opcode = OpCode.SLE;
	}

	@Override
	public String prettyPrint() {
		return "(SLE " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
	}

}
