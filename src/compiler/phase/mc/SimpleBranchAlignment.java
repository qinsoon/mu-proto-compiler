package compiler.phase.mc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import uvm.CompiledFunction;
import uvm.mc.MCBasicBlock;

public class SimpleBranchAlignment extends AbstractMCCompilationPhase {

    public SimpleBranchAlignment(String name) {
        super(name);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        System.out.println("*************Trying to schedule a trace for :" + cf.getOriginFunction().getName() + "**************");
        
        // find out all BBs that are in a cycle
        DFS(cf.entryBB);
        
        // try to align branches and serialise code
        branchAlign(cf);
    }
    
    private void branchAlign(CompiledFunction cf) {
        LinkedList<MCBasicBlock> unvisited = new LinkedList<MCBasicBlock>();
        unvisited.addAll(cf.BBs);
        
        MCBasicBlock cur = cf.entryBB;
        
        while (cur != null) {
            unvisited.remove(cur);
        }
    }
    
    private Set<MCBasicBlock> visited = new HashSet<MCBasicBlock>();
    private Stack<MCBasicBlock> currentVisiting = new Stack<MCBasicBlock>();
    
    private Set<MCBasicBlock> nodesInCycle = new HashSet<MCBasicBlock>();
    
    private void DFS(MCBasicBlock node) {
        if (currentVisiting.contains(node)) {
            // we have a cycle
            int start = currentVisiting.indexOf(node);
            
            System.out.println("Possible cycle is:");
            
            for (int i = start; i < currentVisiting.size(); i++) {
                System.out.print(i + currentVisiting.get(i).getName() + "->");
                nodesInCycle.add(currentVisiting.get(i));
            }
            System.out.print(start + node.getName());
            System.out.println();
        } else {
            visited.add(node);
            currentVisiting.push(node);
            
            for (MCBasicBlock bb : node.getSuccessor()) {
                DFS(bb);
            }
            
            currentVisiting.pop();
        }
    }

}
