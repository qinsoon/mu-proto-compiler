package uvm.inst;

import uvm.Value;

public class InstInternalPrintPtr extends AbstractInternalInstruction {
	Value v;
	
	public InstInternalPrintPtr(Value v) {
		this.v = v;
	}
	
	public Value getValue() {
		return v;
	}
	
	@Override
	public boolean needsToExpandIntoRuntimeCall() {
		return true;
	}
	
	@Override
	public String prettyPrint() {
		return "PRINTPTR " + v.prettyPrint();
	}

}
