package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;

public class InstLandingPad extends Instruction {

	public InstLandingPad() {
		super();
		this.opcode = OpCode.LANDPAD;
	}
	
	@Override
	public String prettyPrint() {
		return "(LANDINGPAD)";
	}

	@Override
	public boolean needsToExpandIntoRuntimeCall() {
		return true;
	}
}
