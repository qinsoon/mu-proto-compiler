package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;
import compiler.util.Pair;

public class ReplaceRegisterOperand extends AbstractMCCompilationPhase {

    public ReplaceRegisterOperand(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        for (MCBasicBlock bb : cf.BBs) {
        	List<AbstractMachineCode> newMC = new ArrayList<AbstractMachineCode>();
        	
            for (AbstractMachineCode mc : bb.getMC()) {
            	// check if any insertion code
            	if (cf.regMoveCodeInsertion.containsKey(mc.sequence)) {
            		List<Pair<Interval, Interval>> list = cf.regMoveCodeInsertion.get(mc.sequence);
            		
            		// reverse order
            		for (int i = list.size() - 1; i >= 0; i--) {
            			Pair<Interval, Interval> p = list.get(i);
            			
            			MCOperand src = null;
            			if (p.getFirst().getSpill() != null) {
            				src = p.getFirst().getSpill();
            			} else if (p.getFirst().getPhysicalReg() != null) {
            				src = p.getFirst().getPhysicalReg();
            			} else {
            				UVMCompiler.error("error on getting physical reg/spill location for " + p.getFirst().prettyPrint());
            			}
            			
            			MCOperand dst = null;
            			if (p.getSecond().getSpill() != null) {
            				dst = p.getSecond().getSpill();
            			} else if (p.getSecond().getPhysicalReg() != null) {
            				dst = p.getSecond().getPhysicalReg();
            			} else {
            				UVMCompiler.error("error on getting physical reg/spill location for " + p.getSecond().prettyPrint());
            			}
            			
            			if (src == dst)
            				continue;
            			
            			if (p.getFirst().getOrig().getDataType() == MCRegister.DATA_GPR) {
                			AbstractMachineCode mov = UVMCompiler.MCDriver.genMove(dst, src);
                			mov.setComment(p.getFirst().getOrig().prettyPrint());
                			verboseln("inserting code at " + mc.sequence + " for " + p.getFirst().getOrig().prettyPrint() + " -> " + p.getSecond().getOrig().prettyPrint());
                			verboseln("  " + mov.prettyPrintREPOnly());
                			newMC.add(mov);
            			} else if (p.getFirst().getOrig().getDataType() == MCRegister.DATA_DP) {
            				AbstractMachineCode mov = UVMCompiler.MCDriver.genDPMove(dst, src);
            				mov.setComment(p.getFirst().getOrig().prettyPrint());
                			verboseln("inserting code at " + mc.sequence + " for " + p.getFirst().getOrig().prettyPrint() + " -> " + p.getSecond().getOrig().prettyPrint());
                			verboseln("  " + mov.prettyPrintREPOnly());
            				newMC.add(mov);
            			} else {
            				UVMCompiler.error("unimplemented for other data types than GPR types");
            			}
            			

            		}
            	}
            	
            	verboseln("replacing register: seq=" + mc.sequence + ", " + mc.prettyPrintOneline());
                for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                    MCOperand op = mc.getOperand(i);
                    if (op instanceof MCRegister) {
                        Interval interval = cf.intervals.get(((MCRegister) op).REP());
                        
                        if (!interval.isFixed()) {
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
	                        	System.out.println(orig.isFixed());
	                        	UVMCompiler.error("failed to replace register op");
	                        }
                        
                        }
                    }
                }
                
                MCRegister reg = mc.getDefineAsReg();
                if (reg != null) {
                    Interval regInterval = cf.intervals.get(reg.REP());
                    
                    if (!regInterval.isFixed()) {
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
                
                newMC.add(mc);
            }
            
            bb.setMC(newMC);
        }
    }
}
