package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.MCBasicBlock;
import compiler.phase.CompilationPhase;

public class GenMovForPhi extends CompilationPhase {

    public GenMovForPhi(String name) {
        super(name);
    }
    
    @Override
    public void execute() {
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            
            for (MCBasicBlock bb : cf.BBs) {
                for (MCBasicBlock p : bb.getPredecessors()) {
                    
                }
            }
        }
    }
}
