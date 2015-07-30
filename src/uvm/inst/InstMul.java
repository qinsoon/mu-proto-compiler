package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstMul extends AbstractTypedBinop {

	public InstMul(Type type, Value op1, Value op2) {
		super(type, op1, op2);
		opcode = OpCode.MUL;
	}

	@Override
	public String prettyPrint() {
		return "(MUL " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
	}

}
