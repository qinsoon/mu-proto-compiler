package uvm.inst;

import java.util.List;

import uvm.FunctionSignature;
import uvm.Instruction;

public abstract class AbstractCall extends Instruction {
	String func;
	List<uvm.Value> arguments;
	
	public AbstractCall(String func, List<uvm.Value> arguments) {
		this.func = func;
		this.arguments = arguments;
	}
	
	public final String getFunc() {
		return func;
	}

    public final List<uvm.Value> getArguments() {
        return arguments;
    }
    
    public abstract FunctionSignature getSig();
}
