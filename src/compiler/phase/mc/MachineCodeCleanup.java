package compiler.phase.mc;

import java.util.LinkedList;
import java.util.Queue;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import uvm.mc.MCRegister;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

/**
 * 1. remove redundant mov and phi instruction
 * 2. serialize code (for output)
 * 
 * @author Yi
 * 
 */
public class MachineCodeCleanup extends AbstractMCCompilationPhase {

    public MachineCodeCleanup(String name) {
        super(name);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        System.out.println("\n======Machine Code Cleanup======");
        
        LinkedList<MCBasicBlock> unvisited = new LinkedList<MCBasicBlock>();
        unvisited.addAll(cf.BBs);
        
        MCBasicBlock cur = cf.entryBB;
        MCLabel delayingLabel = null;
        
        while (cur != null) {
            System.out.println("dealing with BB #" + cur.getName());
            unvisited.remove(cur);
            
            for (AbstractMachineCode mc : cur.getMC()) {
                // if this mc is not redundant, add to finalMC
                if (!isRedundant(mc)) {
                    cf.finalMC.add(mc);
                    
                    // whenever we adding a mc, we check if we have any delaying label
                    // if so, attach delaying label to the mc, and clear delayingLabel
                    // this code also appears once more later
                    if (checkDelayingLabel(mc, delayingLabel))
                        delayingLabel = null;
                }
                else {
                    // this mc will get deleted, but if it has a label, we will keep the label
                    if (mc.getLabel() != null) {
                        if (delayingLabel != null) {
                            UVMCompiler.error("we have a delaying label #" + delayingLabel.getName() + " but trying to adding another label #" + mc.getLabel().getName());
                        }
                        delayingLabel = mc.getLabel();
                    }
                }
                    
            }
            
            // find next block
            // if a block is a successor but it is not the jump target
            // then it is next block (fallthrough)
            
            MCBasicBlock oldCur = cur;
            cur = null;
            
            for (MCBasicBlock succ : oldCur.getSuccessor()) {
                if (isFallthrough(oldCur, succ)) {
                    // succ is a fallthrough successor from oldCur
                    
                    if (unvisited.contains(succ)) {
                        // if we havent visited yet, visit it (append it to finalMC)
                        cur = succ;
                        System.out.println(" has a fallthrough successor #" + cur.getName());
                    } else {
                        // we have added code in succ in final MC.
                        // then we need to add one jump inst to branch to succ
                        AbstractMachineCode jmp = UVMCompiler.MCDriver.genJmp(succ.getLabel());
                        
                        cf.finalMC.add(jmp);
                        
                        // whenever we add a code to finalMC, we check label
                        // see comments above
                        if (checkDelayingLabel(jmp, delayingLabel))
                            delayingLabel = null;
                        
                        System.out.print(" has a visited fallthrough successor #" + succ.getName());
                        System.out.println(" adding jmp");
                    }
                }
            }
            
            if (cur == null) {
                for (int i = 0; i < unvisited.size(); i++)
                    if (!isFallthroughBlock(unvisited.get(i))) {
                        cur = unvisited.get(i);
                        System.out.println(" picking a random non-fallthrough BB #" + cur.getName());
                        break;
                    }
            }
            
            if (cur == null && !unvisited.isEmpty()) {
                UVMCompiler.error("cannot find next block, but tmp is not empty");
            }
        }
        
        System.out.println("\nAfter MC cleanup, final code for " + cf.getOriginFunction().getName());
        for (AbstractMachineCode mc : cf.finalMC)
            System.out.println(mc.prettyPrint());
    }
    
    /**
     * returning true if we dealt with this delayingLabel (should set delayingLabel to null after returning)
     * @param mc
     * @param delayingLabel
     * @return
     */
    private boolean checkDelayingLabel(AbstractMachineCode mc, MCLabel delayingLabel) {
        if (delayingLabel != null) {
            // this mc also has a label, then we set delayingLabel name to this label
            // so every jump to delayingLabel, will target this label
            if (mc.getLabel() != null) {
                delayingLabel.setName(mc.getLabel().getName());
            } else
                mc.setLabel(delayingLabel);
            
            return true;
        }
        
        return false;
    }
    
    private boolean isFallthrough(MCBasicBlock bb1, MCBasicBlock bb2) {
        if (bb1.getSuccessor().contains(bb2) && bb2.getPredecessors().contains(bb1)) {
            if (bb1.getMC().isEmpty())
                return true;
            if (!bb1.getLast().isJump())
                return true;
            else if (bb1.getLast().isJump() && !((MCLabel)bb1.getLast().getOperand(0)).equals(bb2.getLabel()))
                return true;
        }
        
        return false;
    }
    
    private boolean isFallthroughBlock(MCBasicBlock bb) {
        for (MCBasicBlock p : bb.getPredecessors()) {
            if (!p.getLast().isJump()) {
                return true;
            }
            if (p.getLast().isJump() && !((MCLabel)p.getLast().getOperand(0)).equals(bb.getLabel())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRedundant(AbstractMachineCode mc) {
        if (!mc.isPhi() && !mc.isMov())
            return false;
        
        if (mc.isPhi()) {
            MCRegister res = mc.getReg().REP();
            if ( mc.getOperand(0) instanceof MCRegister
                    && ((MCRegister)mc.getOperand(0)).REP() == res
                    && mc.getOperand(2) instanceof MCRegister
                    && ((MCRegister)mc.getOperand(2)).REP() == res)
                return true;
            else {
                return false;
            }
        }
        
        if (mc.isMov()) {
            if (mc.getOperand(0) instanceof MCRegister && 
                    ((MCRegister)mc.getOperand(0)).REP() == mc.getReg().REP())
                return true;
        }
        
        return false;
    }
}
