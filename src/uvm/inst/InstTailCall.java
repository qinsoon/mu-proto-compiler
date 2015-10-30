package uvm.inst;

import java.util.List;

import uvm.CompiledFunction;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.OpCode;
import uvm.Value;

public class InstTailCall extends AbstractCall {
	Function callee;
	
	public InstTailCall(Function func, List<Value> arguments) {
		super(func.getName(), arguments);
		this.callee = func;
		this.opcode = OpCode.TAILCALL;
	}

	@Override
	public FunctionSignature getSig() {
		return callee.getSig();
	}
	
	public Function getCallee() {
		return callee;
	}

	@Override
	public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("TAILCALL " + callee.getName() + "(");
        for (uvm.Value v : getArguments()) {
            ret.append(v.prettyPrint());
            ret.append(',');
        }
        ret.append(')');
        return ret.toString();
	}

}
