package compiler.phase.mc.linearscan;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import uvm.CompiledFunction;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;
import compiler.util.OrderedList;

public class LinearScan extends AbstractMCCompilationPhase {

    public LinearScan(String name, boolean verbose) {
        super(name, verbose);
    }
    
    OrderedList<Interval> unhandled = null;
    LinkedList<Interval>  active    = null;
    LinkedList<Interval>  inactive  = null;
    LinkedList<Interval>  handled   = null;
    
    CompiledFunction currentCF;
    
    StackManager stack;

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- linear scan for " + cf.getOriginFunction().getName() + " -----");
        
        currentCF = cf;
        stack = new StackManager();
        
        unhandled = new OrderedList<Interval>(cf.intervals.values(), new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                if (o1.getBegin() < o2.getBegin())
                    return -1;
                else if (o1.getBegin() == o2.getBegin())
                    return 0;
                else return 1;
            }
        });
        
        active   = new LinkedList<Interval>();
        inactive = new LinkedList<Interval>();
        handled  = new LinkedList<Interval>();
        
        while (!unhandled.isEmpty()) {
            Interval current = unhandled.poll();
            int position = current.getBegin();
            
            // check for intervals in active that are handled or inactive
            for (int i = 0; i < active.size(); ) {
                Interval it = active.get(i);
                
                // if it ends before position
                // move it from active to handled
                if (it.getEnd() < position) {
                    active.remove(i);
                    handled.add(it);
                    
                    continue;
                } 
                // if it does not cover position
                // move it from active to inactive
                else if (!it.isLiveAt(position)) {
                    active.remove(i);
                    inactive.add(it);
                    
                    continue;
                }
                
                i++;
            }
            
            // check for intervals in inactive that are handled or active
            for (int i = 0; i < inactive.size(); ) {
                Interval it = inactive.get(i);
                // if it ends before position
                // move it from inactive to handled
                if (it.getEnd() < position) {
                    inactive.remove(i);
                    handled.add(it);
                    
                    continue;
                }
                // if it covers position
                // move it from inactive to active
                else if (it.isLiveAt(position)) {
                    inactive.remove(i);
                    active.add(it);
                    
                    continue;
                }
                
                i++;
            }
            
            // if current is a fixed interval, then we dont need to allocate for it
            if (current.isFixed()) {
                if (current.isLiveAt(position)) {
                    unhandled.remove(current);
                    active.add(current);
                } else {
                    unhandled.remove(current);
                    inactive.add(current);
                }
                continue;
            }
            
            // find a register for current
            boolean succ = tryAllocateFreeReg(current);
            if (!succ) {
                allocateBlockedReg(current);
            }
            
            // check if current has a register assigned
            if (current.getPhysicalReg() != null)
                active.add(current);
        }
    }
    
    private Set<MCRegister> getAllPhysicalRegisters(int dataType) {
        HashSet<MCRegister> physRegisters = new HashSet<MCRegister>();
        
        if (dataType == MCRegister.DATA_GPR) {
            // init physRegisters to all GPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
                MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getGPRName(i), MCRegister.MACHINE_REG, dataType);
                physRegisters.add(reg);
            }
        } else if (dataType == MCRegister.DATA_DP) {
            // init physRegisters to double-precision FPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfFPR(); i++) {
                MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getFPRName(i), MCRegister.MACHINE_REG, dataType);
                physRegisters.add(reg);
            }
        } else if (dataType == MCRegister.DATA_SP) {
            // init physRegisters to single-precision FPRs
            UVMCompiler.error("unimplemented reg alloc for sp FPR");
        } else {
            UVMCompiler.error("unexpected interval data type: MEM");
        }
        
        return physRegisters;
    }

    private boolean tryAllocateFreeReg(Interval current) {
        Set<MCRegister> physRegisters = getAllPhysicalRegisters(current.getDataType());
        
        HashMap<MCRegister, Integer> freeUntilPos = new HashMap<MCRegister, Integer>();
        // set freeUntilPos of all physical registers to max int
        for (MCRegister reg : physRegisters)
            freeUntilPos.put(reg, Integer.MAX_VALUE);
        
        for (Interval it : active)
            freeUntilPos.put(it.getPhysicalReg(), 0);
        
        for (Interval it : inactive) {
            if (it.doesIntersectWith(current)) {
                // freeUntilPos = next intersection of it with current
                freeUntilPos.put(it.getPhysicalReg(), it.nextIntersectionWith(current));
            }
        }
        
        // get the register with the highest freeUntilPos
        MCRegister candidate = null;
        int highestFreeUntilPos = -1;
        for (MCRegister reg : freeUntilPos.keySet()) {
            int i = freeUntilPos.get(reg);
            if (i > highestFreeUntilPos) {
                candidate = reg;
                highestFreeUntilPos = i;
            }
        }
        
        if (highestFreeUntilPos == 0 || highestFreeUntilPos == -1)
            // no register available without spilling
            return false;
        else if (current.getEnd() < highestFreeUntilPos) {
            // register available for the whole interval
            current.setPhysicalReg(candidate);
            verboseln("Allocate " + candidate.prettyPrint() + " to virtual reg " + current.getOrig().prettyPrint());
            return true;
        }
        else {
            // register available for the first part of the interval
            current.setPhysicalReg(candidate);
            verboseln("Allocate " + candidate.prettyPrint() + " to virtual reg " + current.getOrig().prettyPrint());
            
            // split current before freeUntilPos
            unhandled.add(current.splitAt(highestFreeUntilPos));
            return true;
        }
    }
    
    private void allocateBlockedReg(Interval current) {
        Set<MCRegister> physRegisters = getAllPhysicalRegisters(current.getDataType());
        
        HashMap<MCRegister, Integer> nextUsePos = new HashMap<MCRegister, Integer>();
        for (MCRegister reg : physRegisters)
            nextUsePos.put(reg, Integer.MAX_VALUE);
        
        for (Interval it : active) 
            nextUsePos.put(it.getPhysicalReg(), it.nextUseAfter(current.getBegin()));
        for (Interval it : inactive)
            if (it.doesIntersectWith(current))
                nextUsePos.put(it.getPhysicalReg(), it.nextUseAfter(current.getBegin()));
        
        // find register with highest nextUsePos
        MCRegister candidate = null;
        int highestNextUsePos = -1;
        for (MCRegister reg : nextUsePos.keySet()) {
            int i = nextUsePos.get(reg);
            if (i > highestNextUsePos) {
                highestNextUsePos = i;
                candidate = reg;
            }
        }
        
        // if first usage of current is after nextUsePos[reg]
        if (current.firstUse() > highestNextUsePos) {
            // all other intervals are used before current
            // so it is best to spill current itself
            MCMemoryOperand spillSlot = stack.spillInterval(current);
            int firstRegOnlyUse = current.firstRegOnlyUse();
            Interval afterSplit = current.splitAt(firstRegOnlyUse);
            unhandled.add(afterSplit);
        } else {
            // spill intervals that currently block reg
            current.setPhysicalReg(candidate);
            
            // find the interval in active to be split
            Interval toBeSplit = null;
            for (Interval it : active) {
                if (it.getPhysicalReg() == candidate) {
                    toBeSplit = it;
                    break;
                }
            }
            if (toBeSplit == null)
                UVMCompiler.error("cannot find the interval to be split in active set");
            
            // split active interval for reg at position
            // FIXME: position is current.getBegin() ?
            unhandled.add(toBeSplit.splitAt(current.getBegin()));
            
            // split any inactive interval for reg at the end of its lifetime hole
            for (Interval it : inactive) {
                if (it.getPhysicalReg() == candidate) {
                    int liveAgain = it.skipLifetimeHole(current.getBegin());
                    Interval newInterval = it.splitAt(liveAgain);
                    unhandled.add(newInterval);
                }
            }
        }
        
        // make sure that current does not intersect with 
        // the fixed interval for reg
        int intersectWithFixedInterval = -1;
        for (MCRegister reg : currentCF.intervals.keySet()) {
            if (reg == current.getPhysicalReg()) {
                if (intersectWithFixedInterval == -1)
                    intersectWithFixedInterval = current.nextIntersectionWith(currentCF.intervals.get(reg));
                else {
                    UVMCompiler.error("There are more than one fixed interval related with " + reg.prettyPrint() + " in " + currentCF.getOriginFunction().getName());
                }
            }
        }
        
        if (intersectWithFixedInterval != -1) {
            // split current before this intersection
            Interval split = current.splitAt(intersectWithFixedInterval);
            unhandled.add(split);
        }
    }
}
