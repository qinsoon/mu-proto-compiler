package compiler.phase.mc.linearscan;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
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
            if (!bb.getLast().isBranchingCode() && bb.getSuccessor().size() == 1) {
//            	UVMCompiler._assert(bb.getSuccessor().size() == 1, "BB " + bb.getName() + " ends with non-branching code, but it has more than one successors");
            	
            	AbstractMachineCode jmp = UVMCompiler.MCDriver.genJmp(bb.getSuccessor().get(0).getLabel()); 
            	
            	// adding the code to BB and also the CF
            	bb.addMC(jmp);
            	
            	AbstractMachineCode insertAfter = bb.getLast();
            	int index = cf.getMachineCode().indexOf(insertAfter);
            	if (index == cf.getMachineCode().size() - 1)
            		cf.addMachineCode(jmp);
            	else cf.addMachineCode(index + 1, jmp);
            }
		}
	}

}
