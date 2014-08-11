package compiler.phase.mc;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uvm.CompiledFunction;
import uvm.FunctionSignature;
import uvm.MicroVM;
import uvm.mc.LiveInterval;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCRegister;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public class LinearScan extends AbstractMCCompilationPhase {

    public LinearScan(String name, boolean verbose) {
        super(name, verbose);
    }
    
    public static final int RESERVED_GPR_SCRATCH_REGISTERS = 1;
    public static final int RESERVED_FPR_SCRATCH_REGISTERS = 1;
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        // implementing Section 5 of Linear Scan Register Allocation on SSA paper
        verboseln("\nRunning Linear Scan on " + cf.getOriginFunction().getName());
        
        LinkedList<LiveInterval> unhandled = prepareIntervals(cf);
        LinkedList<LiveInterval> active = new LinkedList<LiveInterval>();
        LinkedList<LiveInterval> inactive = new LinkedList<LiveInterval>();
        LinkedList<LiveInterval> handled = new LinkedList<LiveInterval>();
        
        LinkedList<MCRegister> freeGPRs = new LinkedList<MCRegister>();
        LinkedList<MCRegister> freeFPRs = new LinkedList<MCRegister>();
        
        int stackSlot = 0;
        int stackDisp = - UVMCompiler.MC_REG_SIZE_IN_BYTES;
        
        // init free registers
        for (int i = RESERVED_GPR_SCRATCH_REGISTERS; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
            String regName = UVMCompiler.MCDriver.getGPRName(i);
            MCRegister reg = cf.findRegister(regName, MCRegister.MACHINE_REG); 
            if (reg == null) {
                // its a free register
                reg = cf.findOrCreateRegister(regName, MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                freeGPRs.add(reg);
            } else {
                // this register is used by certain mc
                // TODO problem here: if a register is used by a certain mc, its in 'inactive' set.
                // but it won't become a free reg until its interval elapses (we have one less register to use)
                // we should assume that register coalescing already tried to join other symbolic reg to it (but not other machine reg to it)
                LiveInterval interval = cf.intervals.get(reg);
                inactive.add(interval);
            }
        }
        
        for (int i = RESERVED_FPR_SCRATCH_REGISTERS; i < UVMCompiler.MCDriver.getNumberOfFPR(); i++) {
            String regName = UVMCompiler.MCDriver.getFPRName(i);
            MCRegister reg = cf.findRegister(regName, MCRegister.MACHINE_REG);
            if (reg == null) {
                reg = cf.findOrCreateRegister(regName, MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                freeFPRs.add(reg);
            } else {
                LiveInterval interval = cf.intervals.get(reg);
                inactive.add(interval);
            }
        }
        
        if (verbose) {
            System.out.print("free GPRs:");
            for (MCRegister reg : freeGPRs)
                System.out.print(reg.prettyPrint() + " ");
            System.out.println();
            System.out.print("free FPRs:");
            for (MCRegister reg : freeFPRs)
                System.out.print(reg.prettyPrint() + " ");
            System.out.println();
        }
        
        // while unhandled <> {} do
        while (!unhandled.isEmpty()) {
            LiveInterval cur = unhandled.poll();
            verboseln(cur.prettyPrint());
            
            // check for active intervals that expired
            for (int i = 0; i < active.size(); ) {
                LiveInterval li = active.get(i);
                if (li.getEnd() < cur.getBegin()) {
                    // move li to handled and add i.reg to free
                    active.remove(li);
                    handled.add(li);
                    
                    MCRegister becomeFree = li.getReg();
                    if (becomeFree.getDataType() == MCRegister.DATA_GPR)
                        freeGPRs.add(becomeFree);
                    else freeFPRs.add(becomeFree);
                    
                    continue;
                } else if (!li.overlap(cur.getBegin())) {
                    // move i to inactive and add i.reg to free
                    active.remove(li);
                    inactive.add(li);
                    
                    MCRegister becomeFree = li.getReg();
                    if (becomeFree.getDataType() == MCRegister.DATA_GPR)
                        freeGPRs.add(becomeFree);
                    else freeFPRs.add(becomeFree);
                    
                    continue;
                } else i++;                
            }
            
            // check for inactive intervals that expired or become reactive
            for (int i = 0; i < inactive.size(); ) {
                LiveInterval li = inactive.get(i);
                if (li.getEnd() < cur.getBegin()) {
                    // move li to handled
                    inactive.remove(li);
                    handled.add(li);
                    
                    continue;
                } else if (li.overlap(cur.getBegin())) {
                    // move li to active and remove i.reg from free
                    inactive.remove(li);
                    active.add(li);
                    
                    MCRegister remove = li.getReg();
                    if (remove.getDataType() == MCRegister.DATA_GPR)
                        freeGPRs.remove(remove);
                    else freeFPRs.remove(remove);
                    
                    continue;
                } else i++;
            }
            
            // if current interval is already a physical register
            // we dont need to anything
            if (cur.getReg().REP().getREPType() == MCRegister.MACHINE_REG)
                continue;
            
            // collect available registers in f
            
            verboseln("Trying assign reg for " + cur.getReg().REP().getName() + " of dataType= " + cur.getReg().REP().getDataType() + ", HLLOp=" + cur.getReg().REP().prettyPrintHLLOp());
            
            // f <- free
            LinkedList<MCRegister> f = new LinkedList<MCRegister>();
            if (cur.getReg().REP().getDataType() == MCRegister.DATA_GPR)
                f.addAll(freeGPRs);
            else f.addAll(freeFPRs);
            
            // for each interval i in inactive that overlaps cur
            // do f <- f - {i.reg}
            for (LiveInterval i : inactive) {
                if (i.overlap(cur)) {
                    MCRegister reg = i.getReg().REP();
                    if (f.contains(reg))
                        f.remove(reg);
                }
            }
            
            // for each fixed interval i in unhandled that overlaps cur
            // do f <- f - {i.reg}
            for (LiveInterval i : unhandled) {
                if (i.overlap(cur)) {
                    MCRegister reg = i.getReg().REP();
                    if (f.contains(reg))
                        f.remove(reg);
                }
            }
            
            // select a register from f
            if (f.isEmpty()) {
                // TODO: compute weight and decide which register to spill first
                // missing implementation here
                
                // spilling register here
                MCRegister toBeSpilled = cur.getReg().REP();
//                
//                // assign a memory location
//                MCDispMemoryOperand mem = new MCDispMemoryOperand();
//                mem.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
//                if (toBeSpilled.getDataType() == MCRegister.DATA_GPR) {
//                    mem.setDisp(stackDisp);
//                    mem.setSize((byte) 8);
//                    stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
//                    stackSlot ++;
//                } else if (toBeSpilled.getDataType() == MCRegister.DATA_DP || toBeSpilled.getDataType() == MCRegister.DATA_SP) {
//                    UVMCompiler.error("spilling floating pointer registers to memory");
//                } else {
//                    UVMCompiler.error("spilling unknown register type to memory");
//                }
//                
//                verboseln(toBeSpilled.prettyPrint() + " spilled to " + mem.prettyPrint());
                toBeSpilled.setSPILL(true);
            } else {
                MCRegister freeReg = f.poll();
                
                verboseln("assigning " + cur.getReg().REP().prettyPrint() + " to " + freeReg.prettyPrint());
                cur.getReg().REP().setREP(freeReg);
                
                // free <- free - {cur.reg}
                if (freeReg.getDataType() == MCRegister.DATA_GPR)
                    freeGPRs.remove(freeReg);
                else freeFPRs.remove(freeReg);
                
                // move cur to active
                active.add(cur);
            }
        }
    
        if (verbose) {
            System.out.println("\nAfter linear scan:");
            System.out.println(cf.prettyPrint());
        }
    }

    private LinkedList<LiveInterval> prepareIntervals(CompiledFunction cf) { 
        LinkedList<LiveInterval> all = new LinkedList<LiveInterval>();
        
        // before sorting
        for (MCRegister reg : cf.intervals.keySet()) {
            LiveInterval i = cf.intervals.get(reg.REP());
            for (List<LiveInterval.Range> list : i.getRanges().values()) {
                for (LiveInterval.Range r : list) {
                    if (i.getBegin() > r.getStart())
                        i.setBegin(r.getStart());
                    if (i.getEnd() < r.getEnd())
                        i.setEnd(r.getEnd());
                }
            }
            if (!all.contains(i))
                all.add(i);
        }
        
        // bubble sort:
        // sort intervals on 'begin' by increasing order
        int n = all.size();
        boolean swapped;
        do {
            swapped = false;
            for (int i = 1; i <= n - 1; i++) {
                if (all.get(i-1).getBegin() > all.get(i).getBegin()) {
                    //swap
                    LiveInterval tmp = all.get(i-1);
                    all.set(i-1, all.get(i));
                    all.set(i, tmp);
                    swapped = true;
                }
            }
            n = n - 1;
        } while (swapped);
        
        return all;
    }
}
