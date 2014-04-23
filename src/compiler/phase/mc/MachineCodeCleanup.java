package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.MicroVM;
import compiler.phase.CompilationPhase;

/**
 * 1. remove redundant mov and phi instruction
 * 2. serialize code (for output)
 * 
 * @author Yi
 * 
 */
public class MachineCodeCleanup extends CompilationPhase {

    public MachineCodeCleanup(String name) {
        super(name);
    }

    @Override
    public void execute() {
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            
        }
    }
}
