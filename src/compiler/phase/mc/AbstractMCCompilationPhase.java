package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.MicroVM;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public abstract class AbstractMCCompilationPhase extends AbstractCompilationPhase {

    public AbstractMCCompilationPhase(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    public final void execute() {
    	if (UVMCompiler.TIMING_COMPILATION)
    		recordStart();
    	
    	verboseln("=========== " + name + " ===========\n");
    	
    	preChecklist();
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            visitCompiledFunction(cf);
        }
        postChecklist();
        
        if (UVMCompiler.TIMING_COMPILATION)
        	recordEnd();
    }

    protected abstract void visitCompiledFunction(CompiledFunction cf);
}
