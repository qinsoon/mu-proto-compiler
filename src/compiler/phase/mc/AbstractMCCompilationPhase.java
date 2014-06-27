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
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            visitCompiledFunction(cf);
        }
    }

    protected abstract void visitCompiledFunction(CompiledFunction cf);
}
