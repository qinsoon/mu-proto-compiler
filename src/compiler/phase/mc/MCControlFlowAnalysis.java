package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.Label;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public class MCControlFlowAnalysis extends AbstractMCCompilationPhase{

    public MCControlFlowAnalysis(String name, boolean verbose) {
        super(name, verbose);
    }

    public static final String FALL_THROUGH_BLOCK = "fallthrough";
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- reconstruction BB for " + cf.getOriginFunction().getName() + " -----");
        // if a BB doesnt have a label with it, use an indexed name: bbName + bbIndex
        String bbName = FALL_THROUGH_BLOCK;
        int bbIndex = 0;
        
        MCBasicBlock curBB = null;
        
        for (AbstractMachineCode mc : cf.getMachineCode()) {
            verboseln("scanning mc : " + mc.prettyPrintOneline());
            if (curBB == null) {
                // start of a BB
                
                if (mc.getLabel() != null) {
                    curBB = new MCBasicBlock(mc.getLabel());
                    verboseln("set curBB = " + curBB.getName());
                } else {
                    curBB = new MCBasicBlock(bbName + bbIndex);
                    mc.setLabel(curBB.getLabel());
                    bbIndex ++;
                }
                
                if (cf.entryBB == null)
                    cf.entryBB = curBB;
                
                cf.BBs.add(curBB);
            } 
            else if (curBB != null && mc.getLabel() != null) {
                System.out.println("dealing with " + mc.prettyPrint());
                System.out.println("curBB=" + curBB.getName());
                System.out.println("mc=" + mc.prettyPrint());
                UVMCompiler.error("check what happened, probably a fall-through BB in source code. ");
            }
            
            // in the middle of a BB
            curBB.addMC(mc);
                
            // check if a BB ends
            if (mc.isPhi() && curBB.getPhi() == null)
                curBB.setPhi(mc);
            
            if (mc.isBranchingCode()) {
            	verboseln("finished this bb " + curBB.getName());
                curBB = null;
            }
        }            
        
        // collect more info for BBs
        for (int i = 0; i < cf.BBs.size(); i++) {
            MCBasicBlock bb = cf.BBs.get(i);
            
            AbstractMachineCode branch = bb.getLast();
            
            if (branch.isJump()) {
                // jump target is a successor
                MCLabel label = (MCLabel) bb.getLast().getOperand(0);
                for (MCBasicBlock targetBB : cf.BBs)
                    if (targetBB.getLabel().getName().equals(label.getName())) {
                        bb.addSuccessor(targetBB);
                        targetBB.addPredecessors(bb);
                    }
            } 
            
            // in case of conditional jump, next bb is another successor
            if (branch.isCondJump()) {
                // next bb is another successor
                MCBasicBlock next = cf.BBs.get(i + 1);
                bb.addSuccessor(next);
                next.addPredecessors(bb);
            }
            
            if (branch.isCallWithExp()) {
            	MCBasicBlock normal = cf.getBasicBlock(((MCLabel) branch.getOperand(1)).getName());
            	MCBasicBlock exception = cf.getBasicBlock(((MCLabel) branch.getOperand(2)).getName());
            	
            	bb.addSuccessor(normal);
            	bb.addSuccessor(exception);
            	
            	normal.addPredecessors(bb);
            	exception.addPredecessors(bb);
            }
        }
        
        // check if all phi nodes have valid labels
        // e.g. BB1 -> BB2
        //          -> BB3
        // then BB1 would have two branching instructions at the end, the previous step, it will be rewritten into
        // e.g. BB1 -> BB2
        //          -> fallthroughX -> BB3
        // if we have a PHI in BB3, it would expect fallthroughX instead of BB1
        // we find and fix it here
        for (MCBasicBlock bb : cf.BBs) {
        	if (bb.getPhi() != null) {
        		for (AbstractMachineCode mc : bb.getMC()) {
        			if (mc.isPhi()) {
        				for (int i = 1; i < mc.getNumberOfOperands(); i += 2) {
        					MCLabel l = (MCLabel) mc.getOperand(i);
        					
        					boolean foundPredecessor = false;
        					for (MCBasicBlock pre : bb.getPredecessors()) {
        						if (pre.getName().equals(l.getName()))
        							foundPredecessor = true;
        					}
        					
        					if (foundPredecessor)
        						continue;
        					else {
        						// we are gonna need to fix it
        						for (MCBasicBlock pre : bb.getPredecessors()) {
        							if (pre.getName().contains(FALL_THROUGH_BLOCK)) {
        								UVMCompiler._assert(pre.getPredecessors().size() == 1, "a fallthrough block should have exactly 1 predecessor");
        								
        								if (pre.getPredecessors().get(0).getName().equals(l.getName()))
        									mc.setOperand(i, pre.getLabel());
        							}
        						}
        					}
        				}
        			}
        		}
        	}
        }
        
        // print
        if (verbose) {
            for (MCBasicBlock bb : cf.BBs) {
                System.out.println(bb.prettyPrintWithPreAndSucc());
            }
        }
    }
}
