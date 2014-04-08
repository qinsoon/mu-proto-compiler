package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uvm.mc.AbstractMachineCode;
import uvm.mc.LiveInterval;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;

public class CompiledFunction {
    Function origin;
    
    // MC
    public List<AbstractMachineCode> mc = new ArrayList<AbstractMachineCode>();
    public List<AbstractMachineCode> sequenceMC = new ArrayList<AbstractMachineCode>();    
    
    // register live interval
    public HashMap<MCRegister, LiveInterval> intervals = new HashMap<MCRegister, LiveInterval>();
    
    // MC BB
    public List<MCBasicBlock> BBs = new ArrayList<MCBasicBlock>();
    public MCBasicBlock entryBB;
    
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

    public List<AbstractMachineCode> getSequenceMC() {
        return sequenceMC;
    }

    public void addSequenceMC(AbstractMachineCode mc) {
        this.sequenceMC.add(mc);
    }
}
