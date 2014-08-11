package compiler.phase.mc.x64;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class InsertSpillingCode extends AbstractMCCompilationPhase {

    public InsertSpillingCode(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- Trying to insert spilling code for " + cf.getOriginFunction().getName() + " -----");
        
        // x64 doesnt allow two mem ops in an instruction
        // thus we allow at most one mem op in an instruction (such as mov, add)
        // we check every instruction, and modify spilling if necessary
        checkMemOps(cf);
    }

    private void checkMemOps(CompiledFunction cf) {
        for (AbstractMachineCode mc : cf.finalMC) {
            
            // for mov instruction
            // reg and op0 cant both be mem ops
            if (mc.isMov()) {
                
            }
        }
    }
}
