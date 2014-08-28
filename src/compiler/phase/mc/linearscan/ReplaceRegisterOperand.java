package compiler.phase.mc.linearscan;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class ReplaceRegisterOperand extends AbstractMCCompilationPhase {

    public ReplaceRegisterOperand(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        for (MCBasicBlock bb : cf.BBs) {
            for (AbstractMachineCode mc : bb.getMC()) {
            	verboseln("replacing register: " + mc.prettyPrintOneline());
                for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                    MCOperand op = mc.getOperand(i);
                    if (op instanceof MCRegister) {
                        Interval interval = cf.intervals.get(((MCRegister) op).REP());
                        if (interval.getSpill() != null) {
                            mc.setOperand(i, interval.getSpill());
                        } else if (interval.getPhysicalReg() != null) {
                            mc.setOperand(i, interval.getPhysicalReg());
                        }
                    }
                }
                
                MCRegister reg = mc.getDefineAsReg();
                if (reg != null) {
                    Interval regInterval = cf.intervals.get(reg.REP());
                    if (regInterval.getSpill() != null) {
                    	mc.setDefine(regInterval.getSpill());
                    } else if (regInterval.getPhysicalReg() != null) {
                        mc.setDefine(regInterval.getPhysicalReg());
                    }
                }
            }
        }
    }
}
