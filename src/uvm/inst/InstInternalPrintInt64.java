package uvm.inst;

import uvm.Instruction;
import uvm.Value;

public class InstInternalPrintInt64 extends AbstractInternalInstruction {
	Value v;
	
	public InstInternalPrintInt64 (Value v) {
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
		return "PRINTINT64 " + v.prettyPrint();
	}

}
