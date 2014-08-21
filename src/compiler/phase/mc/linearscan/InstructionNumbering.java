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

    public InstructionNumbering(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        ArrayList<MCBasicBlock> topologicalOrder = new ArrayList<MCBasicBlock>();
        HashSet<MCBasicBlock> visited = new HashSet<MCBasicBlock>();
        Stack<MCBasicBlock> dfs = new Stack<MCBasicBlock>();
        
        dfs.push(cf.entryBB);
        
        while (!dfs.isEmpty()) {
            MCBasicBlock cur = dfs.peek();
            System.out.println("peeking " + cur.getName());
            System.out.print("Stack: ");
            for (MCBasicBlock b : dfs)
                System.out.print(b.getName() + ",");
            System.out.println();
            
            // if we have visited cur then pop and proceed the next
            if (visited.contains(cur)) {
                dfs.pop();
                continue;
            }
            
            // check if cur's all predecessors have been visited
            boolean allPredecessorsVisited = true;
            for (MCBasicBlock pred : cur.getPredecessors()) {
                if (!visited.contains(pred) && pred != cur)
                    allPredecessorsVisited = false;
            }
            
            if (allPredecessorsVisited) {
                // visit current block
                cur = dfs.pop();
                visited.add(cur);
                topologicalOrder.add(cur);
                
                for (MCBasicBlock succ : cur.getSuccessor()) {
                    if (!visited.contains(succ)) {
                        dfs.push(succ);
                    }
                }
            } else {
                // otherwise
                MCBasicBlock bubble = dfs.firstElement();
                dfs.remove(bubble);
                dfs.push(bubble);
            }
        }
        
    }
    
    protected void visitCompiledFunctionOld(CompiledFunction cf) {
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

        verboseln("\ninstruction numbering:\n");
        int sequence = 0;
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
