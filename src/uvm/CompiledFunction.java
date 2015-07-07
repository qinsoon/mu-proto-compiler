package uvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import compiler.UVMCompiler;
import compiler.util.DotGraph;
import compiler.util.MultiValueMap;
import compiler.util.Pair;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import uvm.mc.linearscan.LivenessRange;
import compiler.phase.mc.linearscan.*;

public class CompiledFunction {
    Function origin;
    
    // MC
    
    // after CFG is built, if there is any insertion/deletion on machine code (e.g. MachineCodeExpansion)
    // we need both change mc here and mc in MCBasicBlocks
    // FIXME
    private List<AbstractMachineCode> mc = new LinkedList<AbstractMachineCode>();
    
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
    public List<MCRegister> usedParamRegs = new ArrayList<MCRegister>();		// those gets a define USE_POS at 0
    
    public HashMap<Integer, List<Pair<Interval, Interval>>> regMoveCodeInsertion;
    public StackManager stackManager;
    
    int reserveScratchRegs = 0;
    
    public boolean hasSpilledValues() {
    	return reserveScratchRegs != 0;
    }
    
    public int getReserveScratchRegs() {
		return reserveScratchRegs;
	}

	public void setReserveScratchRegs(int reserveScratchRegs) {
		this.reserveScratchRegs = reserveScratchRegs;
	}

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
            str.append(bb.prettyPrintREPOnly());
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
    
    public void addMachineCode(int index, AbstractMachineCode code) {
    	mc.add(index, code);
    }
    
    public void addMachineCode(List<AbstractMachineCode> code) {
        mc.addAll(code);
    }
    
    public void addMachineCode(int index, List<AbstractMachineCode> code) {
    	mc.addAll(index, code);
    }
    
    public void setMachineCode(List<AbstractMachineCode> code) {
    	this.mc = code;
    }
    
    public List<AbstractMachineCode> getMachineCode() {
        return mc;
    }
    
    /**
     * 
     * @return
     */
    public int getNumberOfMachineCodes() {
    	return mc.size();
    }
    
    public int getIndexOfMachineCode(AbstractMachineCode code) {
    	return mc.indexOf(code);
    }
    
    /**
     * get registers that are alive at seq
     * @param sequence
     * @return
     */
    public List<MCRegister> getLiveRegistersAt(int sequence) {
        List<MCRegister> ret = new ArrayList<MCRegister>();
        
        for (Entry<MCRegister, Interval> entry : intervals.entrySet()) {
            if (entry.getValue().isLiveAt(sequence)) {
                ret.add(entry.getKey().REP());
            }
        }
        return ret;
    }
    
    /**
     * get the registers that are:
     * 1. alive before the inst (alive at seq-1)
     * 2. alive after the inst (alive at seq+2)
     * 3. not re-defined at the inst(seq+1)
     * @param sequence
     * @return
     */
    public List<MCRegister> getLiveRegistersThrough(int sequence) {
    	List<MCRegister> aliveBefore = getLiveRegistersAt(sequence - 1);
    	List<MCRegister> aliveAfter  = getLiveRegistersAt(sequence + 2);
    	
    	List<MCRegister> ret = new ArrayList<MCRegister>();
    	
    	for (MCRegister reg : aliveBefore) {
    		if (aliveAfter.contains(reg)) {
    			// check reg is not defined at seq + 1
    			Interval interval = intervals.get(reg);
    			if (!interval.hasDefineAt(sequence+1))
    				ret.add(reg);
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

	public HashMap<MCRegister, Pair<Interval, Interval>> getLiveInAndOutBetween(
			int start, int end) {
		HashMap<MCRegister, Pair<Interval, Interval>> ret = 
				new HashMap<MCRegister, Pair<Interval, Interval>>();
		
		for (MCRegister virtualReg : intervals.keySet()) {
			Interval i = intervals.get(virtualReg);
			Pair<Interval, Interval> pair = Interval.firstAndLastEncountingInterval(i, start, end);
			if (pair.getFirst() != null || pair.getSecond() != null)
				ret.put(virtualReg, pair);
		}
		
		return ret;
	}
	

}
