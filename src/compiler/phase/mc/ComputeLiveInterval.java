package compiler.phase.mc;

import static uvm.mc.LiveInterval.Range.UNKNOWN_END;
import static uvm.mc.LiveInterval.Range.UNKNOWN_START;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.LiveInterval;
import uvm.mc.LiveInterval.Range;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;

import compiler.UVMCompiler;

// TODO this live interval analysis is based on SSA
// however the machine code is not _strictly_ SSA
// E.g. 1. phi mc is not SSA
// ==> as stated in the paper, phi mc is never included in a range, so its fine
// E.g. 2. "add a, b -> a", which is a X86/64 mc, is not SSA
// ==>  this may be NOT fine
public class ComputeLiveInterval extends AbstractMCCompilationPhase {

    public ComputeLiveInterval(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        buildLiveIn(cf);
        buildIntervals(cf);
        
        if (verbose)
            cf.printInterval();
    }
    
    public void buildLiveIn(CompiledFunction cf) {
        verboseln("----- build live-in for " + cf.getOriginFunction().getName() + " -----");
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
            
            if (verbose) {
                System.out.println("\nLive-in for bb " + bb.getName());
                System.out.println(bb.prettyPrint());
                for (MCRegister reg : bb.liveIn)
                    System.out.println(reg.prettyPrint());
            }
        }
    }
    
    private void buildIntervals(CompiledFunction cf) { 
        verboseln("----- build intervals for " + cf.getOriginFunction().getName() + " -----");
        
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
            verboseln(reg.prettyPrint() + ": " + interval.prettyPrint());
            for (MCBasicBlock bb : interval.getRanges().keySet()) {
                List<Range> list = interval.getRange(bb);
                for (Range range : list) {
                    verboseln("range " + range.prettyPrint() + ":");
                    if (range.getStart() == UNKNOWN_START) {
                        verboseln("UNKNOWN START");
                        // if this register is in live-in set, then the start is the first mc of the block
                        if (bb.liveIn.contains(reg.REP())) {
                            verboseln("live-in reg, set start=" + bb.getFirst().sequence);
                            interval.replaceRange(bb, range, bb.getFirst().sequence, range.getEnd());
                        }
                        // otherwise, this register might be implicitly produced by other mc
                        // we set the start to its previous mc
                        else {
                            verboseln("set start=" + range.getEnd());
                            int newStart = range.getEnd();
                            interval.replaceRange(bb, range, newStart, range.getEnd());
                        }
                    }
                    
                    // TODO problem here
                    // %res_reg45: %res_reg45 beg=0 end=0 {body:[9,9[,[8,-1[,}
                    // range [9,9[:
                    // range [8,-1[:
                    // UNKNOWN END, set end=14
                    if (range.getEnd() == UNKNOWN_END) {
                        verboseln("UNKNOWN END, set end=" + bb.getLast().sequence);
                        
                        boolean liveOut = false;
                        for (MCBasicBlock succ : bb.getSuccessor()) {
                            if (succ.liveIn.contains(reg.REP()))
                                liveOut = true;
                        }
                        
                        if (liveOut) {
                            // this register lives till the end of the bb
                            interval.replaceRange(bb, range, range.getStart(), bb.getLast().sequence);
                        } else {
                            // this register doesnt need to live longer than its last appearance
                            interval.replaceRange(bb, range, range.getStart(), range.getStart());
                        }
                    }
                }
            }
        }
    }

    public void addRange(CompiledFunction cf, MCBasicBlock bb, MCRegister reg, int start, int end) {
        verboseln("adding range [" + start + "," + end + "[ for %" + reg.getName());
        
        if (cf.intervals.containsKey(reg)) {
            verboseln(" found intervals:");
            LiveInterval live = cf.intervals.get(reg);
            verboseln(live.prettyPrint());
            live.addRange(bb, start, end);
            verboseln(" -> ");
            verboseln(cf.intervals.get(reg).prettyPrint());
        }
        else {
            verboseln(" create new intervals");
            LiveInterval l = new LiveInterval(reg);
            l.addRange(bb, start, end);
            cf.intervals.put(reg, l);
        }
    }
}
