package compiler.phase.mc.linearscan;

import uvm.CompiledFunction;
import uvm.mc.MCBasicBlock;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class AddingJumpInstruction extends AbstractMCCompilationPhase {

	public AddingJumpInstruction(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		for (MCBasicBlock bb : cf.BBs) {
            if (!bb.getLast().isBranchingCode()) {
            	UVMCompiler._assert(bb.getSuccessor().size() == 1, "BB ends with non-branching code, but it has more than one successors");
            	
            	bb.addMC(UVMCompiler.MCDriver.genJmp(bb.getSuccessor().get(0).getLabel()));
            }
		}
	}

}
