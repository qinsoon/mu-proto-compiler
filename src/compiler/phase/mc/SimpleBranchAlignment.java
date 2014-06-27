package compiler.phase.mc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;

public class SimpleBranchAlignment extends AbstractMCCompilationPhase {

    public SimpleBranchAlignment(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- Trying to schedule a trace for :" + cf.getOriginFunction().getName() + " -----");
        
        // find out all BBs that are in a cycle
        DFS(cf.entryBB);
        
        // try to align branches and serialise code
        branchAlign(cf);
    }
    
    private void branchAlign(CompiledFunction cf) {
        LinkedList<MCBasicBlock> unvisited = new LinkedList<MCBasicBlock>();
        unvisited.addAll(cf.BBs);
        
        MCBasicBlock cur = cf.entryBB;
        
        List<AbstractMachineCode> finalMC = new ArrayList<AbstractMachineCode>();
        
        while (cur != null) {
            verboseln(">>>Examining BB " + cur.prettyPrintWithPreAndSucc());
            
            unvisited.remove(cur);

            MCBasicBlock next = null;
            
            // check the last mc (branching)
            AbstractMachineCode lastMC = cur.getLast();
            
            // for unconditional jump, we simply lay its successor after this BB
            if (lastMC.isUncondJump()) {
                if (cur.getSuccessor().size() != 1) {
                    verboseln(cur.prettyPrintWithPreAndSucc());
                    UVMCompiler.error("BB with unconditional branch should have exactly 1 successor");
                }
                
                // if we haven't laid the successor yet
                if (unvisited.contains(cur.getSuccessor().get(0))) {
                    // only one successor
                    cur.getMC().remove(lastMC);
                    if (lastMC.getLabel() != null) {
                        AbstractMachineCode nop = UVMCompiler.MCDriver.genNop();
                        AbstractMachineCode.replaceMC(lastMC, nop);
                        cur.addMC(nop);
                    }
                    // set next bb
                    next = cur.getSuccessor().get(0);
                    
                    verboseln(" remove uncond jump. set next BB as " + next.getName());
                }
            } 
            // for conditional jump, we need to identify which successor should be laid immediate-after
            else if (lastMC.isCondJump()) {
                if (cur.getSuccessor().size() != 2) {
                    verboseln(cur.prettyPrintWithPreAndSucc());
                    UVMCompiler.error("BB with conditional branch should have exactly 2 successors");
                }
                
                // we need to decide which to lay next and which to branch to
                MCBasicBlock succ1 = cur.getSuccessor().get(0);
                MCBasicBlock succ2 = cur.getSuccessor().get(1);
                
                verboseln(" cond jump to 1." + succ1.getName() + ", 2." + succ2.getName());
                
                // we use a simple heuristic: if one succ is in a loop and the other is not
                // then we put the in-loop one after this
                MCBasicBlock succFallThrough = null;
                MCBasicBlock succBranchTo = null;
                if (nodesInCycle.contains(succ1) && !nodesInCycle.contains(succ2)) {
                    succFallThrough = succ1;
                    succBranchTo = succ2;
                } else if (nodesInCycle.contains(succ2) && !nodesInCycle.contains(succ1)) {
                    succFallThrough = succ2;
                    succBranchTo = succ1;
                } else {
                   String originalBranchTo = ((MCLabel) lastMC.getOperand(0)).getName();
                   if (succ1.getName().equals(originalBranchTo)) {
                       succBranchTo = succ1;
                       succFallThrough = succ2;
                   } else {
                       succBranchTo = succ2;
                       succFallThrough = succ1;
                   }
                }
                
                verboseln(" fall through to " + succFallThrough.getName() + ", branch to " + succBranchTo.getName());
                
                if (unvisited.contains(succFallThrough)) {             
                    // adjust code
                    String originalBranchTo = ((MCLabel) lastMC.getOperand(0)).getName();
                    if (!succBranchTo.getName().equals(originalBranchTo)) {
                        AbstractMachineCode newBr = UVMCompiler.MCDriver.genOppositeCondJump(lastMC);
                        newBr.setOperand(0, succBranchTo.getLabel());
                        AbstractMachineCode.replaceMC(lastMC, newBr);
                        
                        cur.getMC().remove(lastMC);
                        cur.addMC(newBr);
                        
                        verboseln(" need to adjust branch code");
                    }
                    
                    // next
                    next = succFallThrough;
                }
            }
            else if (!lastMC.isBranchingCode()) {
                // fallthrough to next block
                if (cur.getSuccessor().size() != 1) {
                    System.out.println(cur.prettyPrintWithPreAndSucc());
                    UVMCompiler.error("no branch mc at the end, but has more than one successor");
                }
                
                verboseln(" originally a fallthrough bb");
                
                MCBasicBlock succ = cur.getSuccessor().get(0);
                if (unvisited.contains(succ)) {
                    next = succ;
                } else {
                    AbstractMachineCode jmp = UVMCompiler.MCDriver.genJmp(succ.getLabel());
                    cur.addMC(jmp);
                    verboseln(" the bb already placed, so add jump");
                }
            }
            
            if (next == null && !unvisited.isEmpty()) {
                next = unvisited.get(0);
                verboseln(" we havent decided next block. get a random one from unvisited: " + next.getName());
            }
            
            finalMC.addAll(cur.getMC());
            cur = next;
        }
        
        if (verbose) {
            System.out.println("----- SimpleBranchAlignment: " + cf.getOriginFunction().getName() + " -----");
            for (AbstractMachineCode mc : finalMC)
                System.out.println(mc.prettyPrintOneline());
        }
        
        cf.finalMC.addAll(finalMC);
    }

    private Set<MCBasicBlock> visited = new HashSet<MCBasicBlock>();
    private Stack<MCBasicBlock> currentVisiting = new Stack<MCBasicBlock>();
    
    private Set<MCBasicBlock> nodesInCycle = new HashSet<MCBasicBlock>();
    
    private void DFS(MCBasicBlock node) {
        if (currentVisiting.contains(node)) {
            // we have a cycle
            int start = currentVisiting.indexOf(node);
            
            verboseln("Possible cycle is:");
            
            for (int i = start; i < currentVisiting.size(); i++) {
                verbose(i + currentVisiting.get(i).getName() + "->");
                nodesInCycle.add(currentVisiting.get(i));
            }
            verbose(start + node.getName());
            verboseln();
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
