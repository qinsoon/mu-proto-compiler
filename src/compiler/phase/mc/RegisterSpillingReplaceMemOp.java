package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;

public class RegisterSpillingReplaceMemOp extends AbstractMCCompilationPhase {

    public RegisterSpillingReplaceMemOp(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- Trying to replace spilled registers with mem op for " + cf.getOriginFunction().getName() + " -----");
        
        for (AbstractMachineCode mc : cf.finalMC) {
            for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                MCOperand op = mc.getOperand(i);
                if (op instanceof MCRegister && ((MCRegister) op).REP().isSpilled()) {
                    verboseln(mc.prettyPrintOneline());
                    MCRegister reg = ((MCRegister)op).REP();
                    verboseln("op:" + op.prettyPrint() + " is spilled to " + reg.SPILL().prettyPrint());
                    mc.setOperand(i, reg.SPILL());
                }
            }
            
            if (mc.getDefine() != null) {
                MCOperand op = mc.getDefine();
                if (op instanceof MCRegister && ((MCRegister)op).REP().isSpilled()) {
                    verboseln(mc.prettyPrintOneline());
                    MCRegister reg = ((MCRegister)op).REP();
                    verboseln("op:" + op.prettyPrint() + " is spilled to " + reg.SPILL().prettyPrint());
                    mc.setDefine(reg.SPILL());
                }
            }
        }
    }
}
