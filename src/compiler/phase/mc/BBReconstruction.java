package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public class BBReconstruction extends AbstractMCCompilationPhase{
    private static final boolean VERBOSE = true;
    
    public BBReconstruction(String name) {
        super(name);
    }

    public static final String FALL_THROUGH_BLOCK = "fallthrough";
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        // if a BB doesnt have a label with it, use an indexed name: bbName + bbIndex
        String bbName = FALL_THROUGH_BLOCK;
        int bbIndex = 0;
        
        MCBasicBlock curBB = null;
        
        for (AbstractMachineCode mc : cf.mc) {
            if (curBB == null) {
                // start of a BB
                
                if (mc.getLabel() != null) {
                    curBB = new MCBasicBlock(mc.getLabel());
                } else {
                    curBB = new MCBasicBlock(bbName + bbIndex);
                    bbIndex ++;
                }
                
                if (cf.entryBB == null)
                    cf.entryBB = curBB;
                
                cf.BBs.add(curBB);
            } else if (curBB != null && mc.getLabel() != null) {
                System.out.println("dealing with " + mc.prettyPrint());
                UVMCompiler.error("check what happened, probably a fall-through BB in source code. ");
            }
            
            // in the middle of a BB
            curBB.addMC(mc);
                
            // check if a BB ends
            if (mc.isPhi() && curBB.getPhi() == null)
                curBB.setPhi(mc);
            
            if (mc.isBranchingCode()) {
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
        }
        
        // print
        if (VERBOSE) {
            for (MCBasicBlock bb : cf.BBs) {
                System.out.println(bb.prettyPrintWithPreAndSucc());
            }
        }
    }
}
