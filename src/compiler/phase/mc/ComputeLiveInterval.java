package compiler.phase.mc;

import java.util.ArrayList;

import static uvm.mc.LiveInterval.Range.UNKNOWN_START;
import static uvm.mc.LiveInterval.Range.UNKNOWN_END;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.LiveInterval;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.LiveInterval.Range;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

// TODO this live interval analysis is based on SSA
// however the machine code is not _strictly_ SSA
// E.g. 1. phi mc is not SSA
// ==> as stated in the paper, phi mc is never included in a range, so its fine
// E.g. 2. "add a, b -> a", which is a X86/64 mc, is not SSA
// ==>  this may be NOT fine
public class ComputeLiveInterval extends AbstractMCCompilationPhase {

    public ComputeLiveInterval(String name) {
        super(name);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        buildLiveIn(cf);
        buildIntervals(cf);
        
        cf.printInterval();;
    }
    
    public void buildLiveIn(CompiledFunction cf) {
        // arbitrary order
        for (MCBasicBlock bb : cf.BBs) {
            Set<MCRegister> defined = new HashSet<MCRegister>();
            for (AbstractMachineCode mc : bb.getMC()) {
                for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                    MCOperand op = mc.getOperand(i);
                    if (op instanceof uvm.mc.MCRegister && !defined.contains(((uvm.mc.MCRegister) op).REP())) 
                        // if this mc uses a register which is not defined within this basic block, then it is a live-in register
                        if (!bb.liveIn.contains( ((MCRegister)op).REP()))
                            bb.liveIn.add(((MCRegister) op).REP());
                }
                
                if (mc.getReg() != null) {
                    defined.add(mc.getReg().REP());                    
                }
            }
            
            System.out.println("\nLive-in for bb " + bb.getName());
            System.out.println(bb.prettyPrint());
            for (MCRegister reg : bb.liveIn)
                System.out.println(reg.prettyPrint());
        }
    }
    
    private void buildIntervals(CompiledFunction cf) {        
        for (MCBasicBlock bb : cf.BBs) {
            // in reverse order
            for (int i = bb.getMC().size() - 1; i >= 0; i--) {
                AbstractMachineCode mc = bb.getMC().get(i);
                
                for (int j = 0; j < mc.getNumberOfOperands(); j++) {
                    MCOperand op = mc.getOperand(j);
                    // this mc uses a register, so the register has a range that ends with this mc
                    if (op instanceof MCRegister) {
                        addRange(cf, bb, ((MCRegister) op).REP(), UNKNOWN_START, mc.sequence);
                    }
                }
                
                if (mc.getReg() != null) {
                    if (mc.sequence == cf.getMachineCode().size() - 1) {
                        System.out.println("mc sequence: " + mc.sequence);
                        System.out.println("mc: " + mc.prettyPrint());
                        UVMCompiler.error("defining a reg in the last mc (shouldnt be)");
                    }
                    
                    // this mc defines a register, so the register has a range that starts with _next_ mc
                    if (mc.sequence + 1 <= bb.getLast().sequence)
                        addRange(cf, bb, mc.getReg().REP(), mc.sequence + 1, UNKNOWN_END);
                }
            }
        }
        
        // there might be some ranges with UNKNOWN_START/UNKNOWN_END
        for (MCRegister reg : cf.intervals.keySet()) {
            LiveInterval interval = cf.intervals.get(reg.REP());
            for (Range range : interval.getRanges()) {
                if (range.getStart() == UNKNOWN_START) {
                    // if this register is in live-in set, then the start is the first mc of the block
                    if (range.getBB().liveIn.contains(reg.REP()))
                        range.setStart(range.getBB().getFirst().sequence);
                    // otherwise, this register might be implicitly produced by other mc
                    // we set the start to its previous mc
                    else range.setStart(range.getEnd());
                }
                
                if (range.getEnd() == UNKNOWN_END) {
                    // this register lives til the end of the bb
                    range.setEnd(range.getBB().getLast().sequence);
                }
            }
        }
    }

    public void addRange(CompiledFunction cf, MCBasicBlock bb, MCRegister reg, int start, int end) {
        System.out.println("adding range [" + start + "," + end + "[ for %" + reg.getName());
        
        if (cf.intervals.containsKey(reg)) {
            System.out.println(" found intervals");
            cf.intervals.get(reg).addRange(bb, start, end);
        }
        else {
            System.out.println(" create new intervals");
            LiveInterval l = new LiveInterval(reg);
            l.addRange(bb, start, end);
            cf.intervals.put(reg, l);
        }
    }
}
