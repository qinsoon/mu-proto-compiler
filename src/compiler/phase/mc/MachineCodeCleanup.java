package compiler.phase.mc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
        
        List<AbstractMachineCode> newFinalMC = new ArrayList<AbstractMachineCode>();
        
        MCLabel delayingLabel = null;
        
        for (AbstractMachineCode mc : cf.finalMC) {
            if (!isRedundant(mc)) {
                newFinalMC.add(mc);
                
                if (checkDelayingLabel(mc, delayingLabel))
                    delayingLabel = null;
            } else {
                if (mc.getLabel() != null) {
                    if (delayingLabel != null) {
                        AbstractMachineCode nop = UVMCompiler.MCDriver.genNop();
                        nop.setLabel(delayingLabel);
                        newFinalMC.add(nop);
                        delayingLabel = null;
                    }
                    
                    delayingLabel = mc.getLabel();
                }
            }
        }
        
        cf.finalMC.clear();
        cf.finalMC.addAll(newFinalMC);
        
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
