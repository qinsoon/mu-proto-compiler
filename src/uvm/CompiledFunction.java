package uvm;

import static uvm.mc.LiveInterval.Range.UNKNOWN_END;
import static uvm.mc.LiveInterval.Range.UNKNOWN_START;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import compiler.UVMCompiler;

import uvm.mc.AbstractMachineCode;
import uvm.mc.LiveInterval;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;
import uvm.mc.LiveInterval.Range;

public class CompiledFunction {
    Function origin;
    
    // MC
    public List<AbstractMachineCode> mc = new ArrayList<AbstractMachineCode>();
    
    // MC BB
    public List<MCBasicBlock> BBs = new ArrayList<MCBasicBlock>();
    public MCBasicBlock entryBB;
    public List<MCBasicBlock> topoloticalBBs = new ArrayList<MCBasicBlock>();
    
    // register live interval
    public HashMap<MCRegister, LiveInterval> intervals = new HashMap<MCRegister, LiveInterval>();

    // register
    private HashMap<String, MCRegister> regs = new HashMap<String, MCRegister>();
    
    public MCRegister findOrCreateRegister(String name, int type) {
        if (regs.containsKey(name))
            return regs.get(name);
        
        MCRegister ret = new MCRegister(name, type);
        regs.put(name, ret);
        return ret;
    }
    
    public MCRegister findRegister(String name, int type) {
        MCRegister ret = regs.get(name);
        if (ret != null) {
            if (ret.getType() == type)
                return ret;
            else UVMCompiler.error(
                    "tring to find register " + name + " with type " + type + " but it was created as type " + ret.getType());
        }
        
        return ret;
    }
    
    public HashMap<String, MCRegister> getRegs() {
        return regs;
    }

    public void setRegs(HashMap<String, MCRegister> regs) {
        this.regs = regs;
    }

    public String prettyPrint() {
        StringBuilder str = new StringBuilder();
        // print
        str.append(getOriginFunction().getName());
        str.append('\n');
        
        // traverse from entryBB
        Queue<MCBasicBlock> traverse = new LinkedList<MCBasicBlock>();
        List<String> traversed = new ArrayList<String>();
        
        traverse.add(entryBB);
        
        while (!traverse.isEmpty()) {
            MCBasicBlock bb = traverse.poll();
            str.append(bb.prettyPrintWithPreAndSucc());
            str.append('\n');
            traversed.add(bb.getName());
            
            for (MCBasicBlock succ : bb.getSuccessor()) {
                if (!traversed.contains(succ.getName()))
                    traverse.add(succ);
            }
        }
        
        return str.toString();
    }
    
    public CompiledFunction(Function origin) {
        this.origin = origin;
    }
    
    public Function getOriginFunction() {
        return origin;
    }
    
    public void addMachineCode(AbstractMachineCode code) {
        mc.add(code);
    }
    
    public void addMachineCode(List<AbstractMachineCode> code) {
        mc.addAll(code);
    }
    
    public List<AbstractMachineCode> getMachineCode() {
        return mc;
    }
    
    public void printInterval() {        
        System.out.println("Live interval for " + getOriginFunction().getName());
        
        int maxRegNameLength = -1;
        for (MCRegister reg : intervals.keySet()) {
            if (reg.getName().length() > maxRegNameLength)
                maxRegNameLength = reg.getName().length();
        }
        
        for (MCRegister reg : intervals.keySet()) {
            System.out.print(String.format("%-"+maxRegNameLength+"s ", reg.getName()));
            
            char[] output = new char[mc.size()];
            for (int i = 0; i < output.length; i++)
                output[i] = 'x';
            LiveInterval interval = intervals.get(reg);
            for (Range range : interval.getRanges()) {
                if (range.getStart() != UNKNOWN_START && range.getEnd() != UNKNOWN_END) {
                    for (int i = range.getStart(); i <= range.getEnd(); i++)
                        output[i] = '-';
                } else {
                    UVMCompiler.error("fml");
                }
            }
            
            System.out.print(output);
            
            System.out.println(" " + reg.prettyPrint());
        }
    }
}
