package uvm;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;

public abstract class Instruction extends IRTreeNode {
    protected Register def;
    
    protected List<Value> operands = new ArrayList<Value>();
    
    protected List<Register> regUses = new ArrayList<Register>();
    
    uvm.Label label;
    
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
    
    public boolean needsToCallRuntimeService() {
    	return false;
    }
}
