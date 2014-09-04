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
            	verboseln("replacing register: seq=" + mc.sequence + ", " + mc.prettyPrintOneline());
                for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                    MCOperand op = mc.getOperand(i);
                    if (op instanceof MCRegister) {
                        Interval interval = cf.intervals.get(((MCRegister) op).REP());
                        Interval orig = interval;
                        
                        // the interval might be split into several smaller intervals
                        // find the interval that contains the mc
                        while (interval != null) {
                        	if (interval.isLiveAt(mc.sequence)) {
                                if (interval.getSpill() != null) {
                                    mc.setOperand(i, interval.getSpill());
                                } else if (interval.getPhysicalReg() != null) {
                                    mc.setOperand(i, interval.getPhysicalReg());
                                }
                                break;
                        	} else {
                        		interval = interval.getNext();
                        	}
                        }

                        if (interval == null) {
                        	System.out.println(orig.prettyPrint());
                        	UVMCompiler.error("failed to replace register op");
                        }
                    }
                }
                
                MCRegister reg = mc.getDefineAsReg();
                if (reg != null) {
                    Interval regInterval = cf.intervals.get(reg.REP());
                    Interval regOrig = regInterval;
                    
                    while (regInterval != null) {
                    	if (regInterval.isLiveAt(mc.sequence + 1) || regInterval.isLiveAt(mc.sequence)) {
                            if (regInterval.getSpill() != null) {
                            	mc.setDefine(regInterval.getSpill());
                            } else if (regInterval.getPhysicalReg() != null) {
                                mc.setDefine(regInterval.getPhysicalReg());
                            }
                            break;
                    	} else {
                    		regInterval = regInterval.getNext();
                    	}
                    }                    

                    if (regInterval == null) {
                    	System.out.println(regOrig.prettyPrint());
                    	UVMCompiler.error("failed to replace register op");
                    }
                }
            }
        }
    }
}
