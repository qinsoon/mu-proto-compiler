package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.MicroVM;
import compiler.phase.AbstractCompilationPhase;

public abstract class AbstractMCCompilationPhase extends AbstractCompilationPhase {

    public AbstractMCCompilationPhase(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    public void execute() {
    	verboseln("=========== " + name + " ===========\n");
    	
    	preChecklist();
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            visitCompiledFunction(cf);
        }
        postChecklist();
    }

    protected abstract void visitCompiledFunction(CompiledFunction cf);
}
