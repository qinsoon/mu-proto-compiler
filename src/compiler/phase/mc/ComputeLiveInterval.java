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
import compiler.phase.CompilationPhase;

public class ComputeLiveInterval extends CompilationPhase {

    public ComputeLiveInterval(String name) {
        super(name);
    }
    
    @Override
    public void execute() {
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            buildLiveIn(cf);
            buildIntervals(cf);
            
            printInterval(cf);
        }
    }
    
    public void buildLiveIn(CompiledFunction cf) {
        // arbitrary order
        for (MCBasicBlock bb : cf.BBs) {
            Set<MCRegister> defined = new HashSet<MCRegister>();
            for (AbstractMachineCode mc : bb.getMC()) {
                for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                    MCOperand op = mc.getOperand(i);
                    if (op instanceof uvm.mc.MCRegister && !defined.contains(op)) 
                        // if this mc uses a register which is not defined within this basic block, then it is a live-in register
                        bb.liveIn.add((MCRegister) op);
                }
                
                if (mc.getReg() != null) {
                    defined.add(mc.getReg());                    
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
                        addRange(cf, bb, (MCRegister) op, UNKNOWN_START, mc.sequence);
                    }
                }
                
                if (mc.getReg() != null)
                    // this mc defines a register, so the register has a range that starts with this mc
                    addRange(cf, bb, mc.getReg(), mc.sequence, UNKNOWN_END);
            }
        }
        
        // there might be some ranges with UNKNOWN_START/UNKNOWN_END
        for (MCRegister reg : cf.intervals.keySet()) {
            for (Range range : cf.intervals.get(reg).getRanges()) {
                if (range.getStart() == UNKNOWN_START) {
                    // if this register is in live-in set, then the start is the first mc of the block
                    if (range.getBb().liveIn.contains(reg))
                        range.setStart(range.getBb().getFirst().sequence);
                    // otherwise, this register might be implicitly produced by other mc
                    // we set the start to its previous mc
                    else range.setStart(range.getEnd());
                }
                
                if (range.getEnd() == UNKNOWN_END) {
                    // this register lives til the end of the bb
                    range.setEnd(range.getBb().getLast().sequence);
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
    
    public void printInterval(CompiledFunction cf) {        
        System.out.println("Live interval for " + cf.getOriginFunction().getName());
        
        int maxRegNameLength = -1;
        for (MCRegister reg : cf.intervals.keySet()) {
            if (reg.getName().length() > maxRegNameLength)
                maxRegNameLength = reg.getName().length();
        }
        
        for (MCRegister reg : cf.intervals.keySet()) {
            System.out.print(String.format("%-"+maxRegNameLength+"s ", reg.getName()));
            
            char[] output = new char[cf.mc.size()];
            for (int i = 0; i < output.length; i++)
                output[i] = 'x';
            LiveInterval interval = cf.intervals.get(reg);
            for (Range range : interval.getRanges()) {
                if (range.getStart() != UNKNOWN_START && range.getEnd() != UNKNOWN_END) {
                    for (int i = range.getStart(); i <= range.getEnd(); i++)
                        output[i] = '-';
                } else {
                    UVMCompiler.error("fml");
                }
            }
            
            System.out.println(output);
        }
    }
}
