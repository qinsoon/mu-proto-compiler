package uvm.inst;

import java.util.List;

import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.mc.MCLabel;

public class InstCCall extends Instruction {
	public static final int CC_DEFAULT = 0;
	
	int callConv;
	FunctionSignature sig;
	String func;

	List<uvm.Value> arguments;
	
	public InstCCall(int callConv, FunctionSignature sig, String func, List<uvm.Value> arguments) {
		this.callConv = callConv;
		this.sig = sig;
		this.func = func;
		this.arguments = arguments;
		
		this.opcode = OpCode.CCALL;
	}

	@Override
	public String prettyPrint() {
		StringBuilder ret = new StringBuilder();
		ret.append("CCALL(");
		for (uvm.Value v : arguments) {
			ret.append(v.prettyPrint());
			ret.append(',');
		}
		ret.append(')');
		ret.append(" cc=");
		ret.append(callConv);
		return ret.toString();
	}

	public int getCallConv() {
		return callConv;
	}

	public FunctionSignature getSig() {
		return sig;
	}

	public String getFunc() {
		return func;
	}

	public List<uvm.Value> getArguments() {
		return arguments;
	}
}
