package uvm.inst;

import java.util.List;

import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.Label;

public abstract class AbstractCall extends Instruction {
	String func;
//	List<uvm.Value> arguments;
	
	public AbstractCall(String func, List<uvm.Value> arguments) {
		this.func = func;
//		this.arguments = arguments;
		for (uvm.Value v : arguments)
			this.operands.add(v);
	}
	
	public final String getFunc() {
		return func;
	}

    public final List<uvm.Value> getArguments() {
    	return this.operands;
    }
    
    public abstract FunctionSignature getSig();
}
