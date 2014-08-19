package compiler.phase.mc.linearscan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import uvm.mc.linearscan.Position;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

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
                        addPosition(cf, bb, ((MCRegister) op).REP(), new Position(mc.sequence, Position.USE, mc, j, false));
                    }
                }
                
                for (int j = 0; j < mc.getNumberOfImplicitUses(); j++) {
                    MCOperand op = mc.getImplicitUse(j);
                    if (op instanceof MCRegister) {
                        addPosition(cf, bb, ((MCRegister) op).REP(), new Position(mc.sequence, Position.USE, mc, -1, false));
                    }
                }
                
                if (mc.getReg() != null) {
                    // this mc defines a register, so the register has a range that starts with _next_ mc
                    if (mc.sequence + 1 <= bb.getLast().sequence)
                        addPosition(cf, bb, mc.getReg().REP(), new Position(mc.sequence + 1, Position.DEFINE, mc, -1, false));
                }
                
                for (int j = 0; j < mc.getNumberOfImplicitDefines(); j++) {
                    MCOperand op = mc.getImplicitDefine(j);
                    if (op instanceof MCRegister) {
                        addPosition(cf, bb, ((MCRegister) op).REP(), new Position(mc.sequence + 1, Position.DEFINE, mc, -1, false));
                    }
                }
            }
            
            // add define if a register is live-in
            for (MCRegister livein : bb.liveIn) {
                addPosition(cf, bb, livein.REP(), new Position(0, Position.DEFINE, null, -1, false));
            }
            
            // add use if a register is live-in for successor blocks
            for (MCBasicBlock succBB : bb.getSuccessor()) {
                for (MCRegister reg : succBB.liveIn) {
                    addPosition(cf, bb, reg.REP(), new Position(bb.getLast().sequence, Position.USE, null, -1, false));
                }
            }
        }
        
        // calc and check intervals
        for (MCRegister reg :cf.intervals.keySet()) {
            // calc
            Interval interval = cf.intervals.get(reg);
            verboseln("computing liveness for " + reg.prettyPrint());
            verboseln(interval.prettyPrint());
            interval.calcLiveness();
            
            // check
            if (interval.getBegin() == -1 || interval.getEnd() == -1) {
                System.out.println("interval for " + reg.prettyPrint() + " doesnt have a valid begin/end:");
                System.out.println(interval.prettyPrint());
                UVMCompiler.exit();
            }
        }
    }

    public void addPosition(CompiledFunction cf, MCBasicBlock bb, MCRegister reg, Position pos) {
        verboseln("adding position [" + pos.getIndex() + "] for " + (pos.isDefine() ? "DEFINE" : "USE"));
        
        if (cf.intervals.containsKey(reg)) {
            verboseln(" found intervals:");
            Interval live = cf.intervals.get(reg);
            verboseln(live.prettyPrint());
            live.addPosition(pos);
            verboseln(" -> ");
            verboseln(cf.intervals.get(reg).prettyPrint());
        }
        else {
            verboseln(" create new intervals");
            Interval l = new Interval(cf.mc.size() * 2, reg.getDataType());
            l.addPosition(pos);
            cf.intervals.put(reg, l);
        }
    }
}
