package compiler.phase.mc.x64;

import uvm.CompiledFunction;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class SpillConstantsToMemory extends AbstractMCCompilationPhase {

    public SpillConstantsToMemory(String name) {
        super(name);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        
    }

}
