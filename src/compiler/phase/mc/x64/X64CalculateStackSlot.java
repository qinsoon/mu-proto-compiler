package compiler.phase.mc.x64;

import uvm.CompiledFunction;
import uvm.StackManager;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64CalculateStackSlot extends AbstractMCCompilationPhase {

	public X64CalculateStackSlot(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
    	verboseln("calculate stack slots for " + cf.getOriginFunction().getName());
		cf.stackManager.calculateStackSlots();
		new X64UVMCallConvention().postRegAllocPatching(cf);
	}

}
