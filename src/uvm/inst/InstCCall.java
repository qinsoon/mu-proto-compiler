package uvm.inst;

import java.util.List;

import compiler.UVMCompiler;

import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.mc.MCLabel;

public class InstCCall extends AbstractCall {
	public static final int CC_DEFAULT = 0;
	
	int callConv;
	FunctionSignature sig;
	
	public InstCCall(int callConv, FunctionSignature sig, String func, List<uvm.Value> arguments) {
		super(func, arguments);
		
		this.callConv = callConv;
		this.sig = sig;
		
		if (sig.getParamTypes().size() != arguments.size()) {
			UVMCompiler.error("InstCCall " + func + " has a different argument count than parameter count");
		}		
		
		this.opcode = OpCode.CCALL;
	}

	@Override
	public String prettyPrint() {
		StringBuilder ret = new StringBuilder();
		ret.append("CCALL " + func + "(");
		for (uvm.Value v : getArguments()) {
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
}
