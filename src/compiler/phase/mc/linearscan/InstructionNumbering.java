package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import compiler.phase.AbstractCompilationPhase;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class InstructionNumbering extends AbstractMCCompilationPhase {
	
	// instructions start at 2. Param registers are defined at 0. So this allows a valid interval for param registers. 
	public static final int INITIAL_SEQUENCE = 2;

    public InstructionNumbering(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        HashMap<MCBasicBlock, Integer> dfsIndex = new HashMap<MCBasicBlock, Integer>();
        ArrayList<MCBasicBlock> topologicalOrder = new ArrayList<MCBasicBlock>();
        int index = 0;

        Stack<MCBasicBlock> dfs = new Stack<MCBasicBlock>();
        dfs.push(cf.entryBB);

        while (!dfs.isEmpty()) {
            MCBasicBlock cur = dfs.pop();
            dfsIndex.put(cur, index);
            topologicalOrder.add(cur);

            index++;

            for (MCBasicBlock succ : cur.getSuccessor()) {
                if (dfsIndex.get(succ) == null)
                    dfs.push(succ);
            }
        }

        verboseln("\ninstruction numbering for " + cf.getOriginFunction().getName() + "\n");
        int sequence = INITIAL_SEQUENCE;
        for (MCBasicBlock cur : topologicalOrder) {
            cf.topologicalBBs.add(cur);
            for (AbstractMachineCode mc : cur.getMC()) {
                verboseln(sequence + ": " + mc.prettyPrint());
                mc.sequence = sequence;
                sequence += 2;
            }
        }
    }
}
