package compiler.phase.mc.x64;

import uvm.CompiledFunction;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64PostRegisterAllocPatching extends AbstractMCCompilationPhase {

	public X64PostRegisterAllocPatching(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		new X64UVMCallConvention().postRegAllocPatching(cf);
	}

}
