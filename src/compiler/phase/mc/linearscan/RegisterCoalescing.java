package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import uvm.mc.linearscan.LivenessRange;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class RegisterCoalescing extends AbstractMCCompilationPhase {

    public RegisterCoalescing(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- Register Coalescing for function " + cf.getOriginFunction().getName() + " -----\n");
        for (MCBasicBlock bb : cf.topologicalBBs) {
//            ArrayList<AbstractMachineCode> newMC = new ArrayList<AbstractMachineCode>();
            
            for (AbstractMachineCode mc : bb.getMC()) {                    
                verboseln("-check mc " + mc.prettyPrintNoLabel());
                boolean joined = false;
                if (mc.isMov()) {
                    // try join
                    if (mc.getOperand(0) instanceof MCRegister &&
                            join(cf, bb,  mc, ((MCRegister) mc.getOperand(0)).REP(), mc.getDefineAsReg().REP())) {
                        // remove this mov mc
                        joined = true;
                    }
                } 
                // join phi during 'GenMovForPhi'
                else if (mc.isPhi()) {
                    // try join
                    if (mc.getOperand(0) instanceof MCRegister &&
                        mc.getOperand(2) instanceof MCRegister &&
                        join(cf, bb, mc, (MCRegister) mc.getOperand(0), mc.getDefineAsReg()) &&
                        join(cf, bb, mc, (MCRegister) mc.getOperand(2), mc.getDefineAsReg())) {
                        // remove this phi mc
                        joined = true;                            
                    }
                    
                    if (!joined) {
                    	// we got a problem here
                    	UVMCompiler.error("failed to join a Phi inst");
                    }
                }
                
                if (joined) {
                    verboseln("->joined");
//                    cf.getMachineCode().remove(mc);
                }
                
                if (!joined) {
                    verboseln("->cant join");
//                    newMC.add(mc);
                }
            }
            
//            bb.setMC(newMC);
        }
        
        if (verbose) {
            System.out.println("\nAfter register coalescing");
            cf.printInterval();
            System.out.println(cf.prettyPrint());
        }
    }
    
    /**
     * join register x to y (removing x, keeping y)
     * @param x
     * @param y
     * @return
     */
    boolean join(CompiledFunction cf, MCBasicBlock bb, AbstractMachineCode mc, MCRegister x, MCRegister y) {
        verboseln(" check if we join x:" + x.REP().prettyPrint() + " and y:" + y.REP().prettyPrint());
        
        if (x.REP().getName().equals(y.REP().getName())) 
            return true;
        
        // we want to keep specific registers
        if (isFixedRegister(x) && !isFixedRegister(y))
            return join(cf, bb, mc, y, x);
        
        // from this point, x, y should be
        // 1) symbolic reg, symbolic reg
        // 2) symbolic reg, machine reg
        // 3) machine reg,  machime reg
        
        if (isNonGeneralPurposeMachineReg(x) || isNonGeneralPurposeMachineReg(y)) {
        	return false;
        }  

        Interval intervalX = cf.intervals.get(x.REP());
        Interval intervalY = cf.intervals.get(y.REP());

        if (intervalX == null || intervalY == null)
        	return false;
        
        // i <- interval[REP(x).n]
        LivenessRange i = intervalX.getLiveness();
        // j <- interval[REP(x).n]
        LivenessRange j = intervalY.getLiveness();
        
//        System.out.println(" i=" + (i == null ? "null" : i.prettyPrint()));
//        System.out.println(" j=" + (j == null ? "null" : j.prettyPrint()));
        verboseln("x=" + intervalX.prettyPrint());
        verboseln("y=" + intervalY.prettyPrint());

//        boolean overlap = i != null && j != null && i.overlap(j);
        boolean overlap = intervalX != null && intervalY != null && intervalX.doesIntersectWith(intervalY);
        boolean compatible = compatible(cf, mc, x, y);
        
        verboseln(" overlap:" + overlap);
        verboseln(" compatible:" + compatible);
        
        if (!overlap && compatible) {
            verboseln(" keeping " + y.REP().prettyPrint() + ", removing " + x.REP().prettyPrint());
            
            // join
            verboseln(" join current range");
            LivenessRange union = LivenessRange.union(i, j);
            
//            if (j == null)
//                cf.intervals.get(y.REP()).addRange(bb, i);
//            else
//                cf.intervals.get(y.REP()).replaceRange(bb, union);
            
            cf.intervals.get(y.REP()).setLivenessRange(union);
            
//            if (i != null)
//                cf.intervals.get(x.REP()).removeRange(bb);
            
            // TODO this part of code might be problematic
//            verboseln(" copy other x ranges to y");
//            for (MCBasicBlock otherBB : cf.intervals.get(x.REP()).getRanges().keySet()) {
//                List<LiveInterval.Range> list = cf.intervals.get(x.REP()).getRange(otherBB);
//                for (LiveInterval.Range r : list)
//                    cf.intervals.get(y.REP()).addRange(otherBB, r);
//            }
            
            cf.intervals.remove(x.REP());
            // end of problem code
            
            x.REP().setREP(y.REP());
            
            return true;
        } 
        
        else return false;
    }
    
    private static boolean compatible(CompiledFunction cf, AbstractMachineCode mc, MCRegister x, MCRegister y) {
        // both GPR or FPU reg
        if (x.getDataType() != y.getDataType()) 
            return false;
        
        // if x is symbolic reg, and y is machine reg, and y is redefined (though it may not be alive/have a live range)
        // we cant join them
        if (isFixedRegister(y) && cf.intervals.get(y).isDefinedDuring(cf.intervals.get(x))) {
        	return false;
        }
        
        // both do not have to be in specific registers
        if (!isFixedRegister(x) && !isFixedRegister(y))
            return true;
        
        // both have to be in the same specific register
        else if (isFixedRegister(x) && isFixedRegister(y)) {
            if (x.equals(y))
                return true;
            else return false;
        }
        
        // x in a specific register and interval of y does not overlap any other intervals
        else if (isFixedRegister(x) && !cf.intervals.get(y).intersectOtherThan(cf.intervals.get(x), mc.sequence))
            return true;
        
        else if (isFixedRegister(y) && !cf.intervals.get(x).intersectOtherThan(cf.intervals.get(y), mc.sequence))
            return true;        
        
        else return false;
    }
    
    private static boolean isMachineRegister(MCRegister reg) {
    	if (reg.getREPType() == MCRegister.MACHINE_REG)
    		return true;
    	return false;
    }
    
    private static boolean isFixedRegister(MCRegister reg) {
        if (reg.getREPType() == MCRegister.OTHER_SYMBOL_REG ||  reg.getREPType() == MCRegister.RES_REG)
            return false;
        else return true;
    }    
    
    public static boolean isNonGeneralPurposeMachineReg(MCRegister reg) {
    	if (reg.getREPType() == MCRegister.MACHINE_REG) {
    		for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
    			if (reg.REP().getName().equals(UVMCompiler.MCDriver.getGPRName(i)))
    				return false;
    		}
    		
    		return true;
    	}
    	
    	return false;
    }
}
