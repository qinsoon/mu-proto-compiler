package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.Instruction;
import uvm.OpCode;

public class InstNewStack extends Instruction {
	Function entryFunc;
	List<uvm.Value> arguments;
	
	public InstNewStack(Function entryFunc, List<uvm.Value> arguments) {
		this.entryFunc = entryFunc;
		this.arguments = arguments;

		this.opcode = OpCode.NEWSTACK;
	}
	
	@Override
	public String prettyPrint() {
		StringBuilder ret = new StringBuilder();
		ret.append("NEWSTACK(");
		ret.append(entryFunc.getName());
		ret.append(",(");
		for (uvm.Value v : arguments) {
			ret.append(v.prettyPrint());
			ret.append(',');
		}
		ret.append("))");
		
		return ret.toString();
	}
	
	@Override
    public boolean needsToExpandIntoRuntimeCall() {
    	return true;
    }
	
	public Function getEntryFunction() {
		return entryFunc;
	}
	
	public List<uvm.Value> getArguments() {
		return arguments;
	}

}
