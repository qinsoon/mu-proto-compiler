package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import compiler.UVMCompiler;
import compiler.util.DotGraph;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import uvm.mc.linearscan.LivenessRange;
import compiler.phase.mc.linearscan.*;

public class CompiledFunction {
    Function origin;
    
    // MC
    public List<AbstractMachineCode> mc = new ArrayList<AbstractMachineCode>();
    
    public List<AbstractMachineCode> finalMC = new ArrayList<AbstractMachineCode>();
    
    public List<AbstractMachineCode> prologue = new ArrayList<AbstractMachineCode>();
    public List<AbstractMachineCode> epilogue = new ArrayList<AbstractMachineCode>();
    
    // MC BB
    public List<MCBasicBlock> BBs = new ArrayList<MCBasicBlock>();
    public MCBasicBlock entryBB;
    public List<MCBasicBlock> topologicalBBs = new ArrayList<MCBasicBlock>();
    
    // register live interval
    public HashMap<MCRegister, Interval> intervals = new HashMap<MCRegister, Interval>();

    // register
    private HashMap<String, MCRegister> regs = new HashMap<String, MCRegister>();
    public List<MCRegister> calleeSavedRegs = new ArrayList<MCRegister>();      // in push order
    
    public MCRegister findOrCreateRegister(String name, int type, int dataType) {
        if (regs.containsKey(name))
            return regs.get(name);
        
        MCRegister ret = new MCRegister(name, type, dataType);
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
                if (!traversed.contains(succ.getName()) && !traverse.contains(succ))
                    traverse.add(succ);
            }
        }
        
        return str.toString();
    }
    
    
    public void printDotFile(String namePostfix) {
        String name = getOriginFunction().getName() + "_" + namePostfix; 
        
        DotGraph graph = new DotGraph(name);
        
        Queue<MCBasicBlock> traverse = new LinkedList<MCBasicBlock>();
        List<String> traversed = new ArrayList<String>();
        
        traverse.add(entryBB);
        
        while(!traverse.isEmpty()) {
            MCBasicBlock bb = traverse.poll();
            System.out.println("visiting " + bb.getName() + ":" + bb.hashCode());
            traversed.add(bb.getName());
            
            List<String> list = new ArrayList<String>();
            for (AbstractMachineCode mc : bb.getMC()) {
                list.add(mc.prettyPrintOneline());
            }
            
            graph.newSequencialSubgraph(bb.getName(), list);
            
            for (MCBasicBlock succ : bb.getSuccessor()) {
                if (!traversed.contains(succ.getName()) && !traverse.contains(succ)) {
                    System.out.println("gonna visit " + succ.getName() + ":" + succ.hashCode());
                    traverse.add(succ);
                }
                
                if (!succ.getMC().isEmpty() && !bb.getMC().isEmpty())
                    graph.newEdge(bb.getLast().prettyPrintOneline(), succ.getFirst().prettyPrintOneline());
            }
        }
        
        graph.output("debug/dot_" + name + ".dot");
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
    
    public List<MCRegister> getLiveRegistersAt(int sequence) {
        List<MCRegister> ret = new ArrayList<MCRegister>();
        
        for (Entry<MCRegister, Interval> entry : intervals.entrySet()) {
            if (entry.getValue().isLiveAt(sequence)) {
                ret.add(entry.getKey().REP());
            }
        }
        return ret;
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
            
            Interval interval = intervals.get(reg);
            System.out.print(interval.prettyPrint());
            
            System.out.println(" " + reg.prettyPrint());
        }
    }

    public List<AbstractMachineCode> getEpilogue() {
        return epilogue;
    }

    public void setEpilogue(List<AbstractMachineCode> epilogue) {
        this.epilogue = epilogue;
    }

    public MCBasicBlock getEntryBB() {
        return entryBB;
    }

    public void setEntryBB(MCBasicBlock entryBB) {
        this.entryBB = entryBB;
    }

    public MCBasicBlock getBasicBlockFor(AbstractMachineCode callMC) {
        for (MCBasicBlock bb : BBs)
            if (bb.getMC().contains(callMC))
                return bb;
        
        return null;
    }
}
