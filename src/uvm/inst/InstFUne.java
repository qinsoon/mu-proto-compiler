package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstFUne extends AbstractTypedBinop {

	public InstFUne(Type type, Value op1, Value op2) {
		super(type, op1, op2);
		opcode = OpCode.FUNE;
	}

	@Override
	public String prettyPrint() {
		return "(FUNE " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
	}

}
