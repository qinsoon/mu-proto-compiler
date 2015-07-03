package uvm;

import java.util.ArrayList;
import java.util.List;

import uvm.mc.MCDPImmediate;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCSPImmediate;
import compiler.UVMCompiler;

public abstract class Instruction extends IRTreeNode {
    protected Register def;
    
    protected List<Value> operands = new ArrayList<Value>();
    
    protected List<Register> regUses = new ArrayList<Register>();
    
    uvm.Label label;
    
    /**
     * some instructions will get expanded to new instructions and old ones get removed. 
     * We store the original ones in the new instructions
     */
    protected Instruction originalInst;
    
    public void setDefReg(Register reg) {
        if (def != null)
            UVMCompiler.error("trying to rewrite def of " + this.prettyPrint());
        
        def = reg;
    }
    
    public void setLabel(Label label) {
        this.label = label;
    }
    
    public Label getLabel() {
        return label;
    }
    
    public Register getDefReg() {
        return def;
    }
    
    public boolean hasDefReg() {
        return def != null;
    }
    
    public List<Register> getRegUses() {
        return regUses;
    }

    public List<Value> getOperands() {
        return operands;
    }
    
    public Instruction getOriginalInst() {
    	return originalInst;
    }
    
    public void setOriginalInst(Instruction inst) {
    	this.originalInst = inst;
    }
    
    /**
     * override this method if the instruction needs to be expanded
     * @return
     */
    public boolean needsToExpandIntoRuntimeCall() {
    	return false;
    }
    
    /**
     * a uIR basic block ends with a branching instruction
     * @return
     */
    public boolean isBranching() {
    	return false;
    }
}
