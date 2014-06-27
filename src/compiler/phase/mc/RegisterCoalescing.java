package compiler.phase.mc;

import java.util.ArrayList;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.LiveInterval;
import uvm.mc.LiveInterval.Range;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;
import compiler.phase.AbstractCompilationPhase;

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
                boolean remove = false;
                if (mc.isMov()) {
                    // try join
                    if (mc.getOperand(0) instanceof MCRegister &&
                            join(cf, bb,  mc, ((MCRegister) mc.getOperand(0)).REP(), mc.getReg().REP())) {
                        // remove this mov mc
                        remove = true;
                    }
                } 
                // join phi during 'GenMovForPhi'
//                else if (mc.isPhi()) {
//                    // try join
//                    if (mc.getOperand(0) instanceof MCRegister &&
//                        mc.getOperand(2) instanceof MCRegister &&
//                        join(cf, bb, mc, (MCRegister) mc.getOperand(0), mc.getReg()) &&
//                        join(cf, bb, mc, (MCRegister) mc.getOperand(2), mc.getReg())) {
//                        // remove this phi mc
//                        remove = true;                            
//                    }
//                }
                
                if (remove) {
                    verboseln("->joined");
//                    cf.getMachineCode().remove(mc);
                }
                
                if (!remove) {
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
        
        // we want to keep specific registers
        if (isSpecificRegister(x) && !isSpecificRegister(y))
            return join(cf, bb, mc, y, x);
        
        // i <- interval[REP(x).n]
        LiveInterval intervalX = cf.intervals.get(x.REP());
        Range i = intervalX.getRange(bb);
        // j <- interval[REP(x).n]
        LiveInterval intervalY = cf.intervals.get(y.REP());
        Range j = intervalY.getRange(bb);
        
//        System.out.println(" i=" + (i == null ? "null" : i.prettyPrint()));
//        System.out.println(" j=" + (j == null ? "null" : j.prettyPrint()));
        verboseln("x=" + intervalX.prettyPrint());
        verboseln("y=" + intervalY.prettyPrint());

//        boolean overlap = i != null && j != null && i.overlap(j);
        boolean overlap = intervalX != null && intervalY != null && intervalX.overlap(intervalY);
        boolean compatible = compatible(cf, mc, x, y);
        
        verboseln(" overlap:" + overlap);
        verboseln(" compatible:" + compatible);
        
        if (!overlap && compatible) {
            verboseln(" keeping " + y.REP().prettyPrint() + ", removing " + x.REP().prettyPrint());
            
            // join
            verboseln(" join current range");
            Range union = Range.union(i, j);
            
            if (j == null)
                cf.intervals.get(y.REP()).addRange(union);
            else
                cf.intervals.get(y.REP()).replaceRange(j.getBB(), union);
            
            if (i != null)
                cf.intervals.get(x.REP()).removeRange(i.getBB());
            
            // TODO this part of code might be problematic
            verboseln(" copy other x ranges to y");
            for (LiveInterval.Range r : cf.intervals.get(x.REP()).getRanges())
                cf.intervals.get(y.REP()).addRange(r);
            
            cf.intervals.get(x.REP()).getRanges().clear();
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
        
        // both do not have to be in specific registers
        if (!isSpecificRegister(x) && !isSpecificRegister(y))
            return true;
        
        // both have to be in the same specific register
        else if (isSpecificRegister(x) && isSpecificRegister(y)) {
            if (x.equals(y))
                return true;
            else return false;
        }
        
        // x in a specific register and interval of y does not overlap any other intervals
        else if (isSpecificRegister(x) && !cf.intervals.get(y).overlapOtherThan(cf.intervals.get(x), mc.sequence))
            return true;
        
        else if (isSpecificRegister(y) && !cf.intervals.get(x).overlapOtherThan(cf.intervals.get(y), mc.sequence))
            return true;
        
        else return false;
    }
    
    private static boolean isSpecificRegister(MCRegister reg) {
        if (reg.getREPType() == MCRegister.OTHER_SYMBOL_REG ||  reg.getREPType() == MCRegister.RES_REG)
            return false;
        else return true;
    }
}
