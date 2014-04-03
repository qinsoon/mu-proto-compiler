package compiler.phase.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import compiler.phase.CompilationPhase;

public class InstructionNumbering extends CompilationPhase {

    public InstructionNumbering(String name) {
        super(name);
    }

    public void execute() {        
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            
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
            
            int sequence = 1;
            for (MCBasicBlock cur : topologicalOrder) {
                for (AbstractMachineCode mc : cur.getMC()) {
                    mc.sequence = sequence;
                    sequence ++;
                }
            }
        }
    }
}
