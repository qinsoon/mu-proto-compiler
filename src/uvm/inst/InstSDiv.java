package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSDiv extends AbstractTypedBinop {

	public InstSDiv(Type type, Value op1, Value op2) {
		super(type, op1, op2);
		opcode = OpCode.SDIV;
	}

	@Override
	public String prettyPrint() {
		return ("SDIV " + op1.prettyPrint() + " " + op2.prettyPrint() + ")");
	}

}
