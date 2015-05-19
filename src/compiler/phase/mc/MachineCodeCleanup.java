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
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
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

    public MachineCodeCleanup(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("\n----- Machine Code Cleanup: " + cf.getOriginFunction().getName() + " -----");
        
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
        
        if (verbose) {
            System.out.println("\nAfter MC cleanup, final code for " + cf.getOriginFunction().getName());
            for (AbstractMachineCode mc : cf.finalMC)
                System.out.println(mc.prettyPrint());
        }
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
            MCOperand define = mc.getDefine();
            MCOperand op1 = mc.getOperand(0);
            MCOperand op2 = mc.getOperand(2);
            
            if (op1 == define && op2 == define)
                return true;
            else {
                return false;
            }
        }
        
        if (mc.isMov()) {
        	MCOperand dst = mc.getDefine();
        	MCOperand src = mc.getOperand(0);
        	
        	if (src instanceof MCRegister && dst instanceof MCRegister &&
        			((MCRegister) src).REP() == ((MCRegister) dst).REP()) {
                return true;
        	} else if (src instanceof MCMemoryOperand && dst instanceof MCMemoryOperand &&
        			src == dst) {
        		return true;
        	}
        }
        
        // if a MC still has temporaries as operands/defs, drop it
        // usually temporaries appear as defs which are not used at all, so register allocator did not allocate a physical reg to it
        // FIXME: should either 1) allocate a physical reg to it, or 2) drop it much earlier
        for (int i = 0; i < mc.getNumberOfOperands(); i++) {
        	MCOperand op = mc.getOperand(i);
        	if (op instanceof MCRegister && ((MCRegister) op).getType() != MCRegister.MACHINE_REG) {
        		return true;
        	}
        }
        	
        if (mc.getDefineAsReg().getType() != MCRegister.MACHINE_REG)
        	return true;
        
        return false;
    }
}
