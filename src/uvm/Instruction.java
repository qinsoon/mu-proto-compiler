package uvm;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;

public abstract class Instruction extends IRTreeNode {
    protected Register def;
    
    protected List<Value> operands = new ArrayList<Value>();
    
    protected List<Register> regUses = new ArrayList<Register>();
    protected List<Instruction> instUses = new ArrayList<Instruction>();
    
    public void setDefReg(Register reg) {
        if (def != null)
            UVMCompiler.error("trying to rewrite def of " + this.prettyPrint());
        
        def = reg;
    }
    
    public Register getDefReg() {
        return def;
    }
    
    public List<Register> getRegUses() {
        return regUses;
    }
    
    public List<Instruction> getInstUses() {
        return instUses;
    }

    public List<Value> getOperands() {
        return operands;
    }
}
