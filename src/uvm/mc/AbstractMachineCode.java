package uvm.mc;

import java.util.List;

public abstract class AbstractMachineCode {
    protected String name;
    protected List<MCOperand> operands;
    
    protected uvm.mc.MCLabel label;
    
    public void setLabel(uvm.mc.MCLabel label) {
        this.label = label;
    }
    
    public MCLabel getLabel() {
        return label;
    }
    
    public MCOperand getOperand(int index) {
        return operands.get(index);
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (label != null)
            ret.append("#" + label.name + ":\n");
        ret.append(prettyPrintNoLabel());
        return ret.toString();
    }
    
    public String prettyPrintNoLabel() {
        StringBuilder ret = new StringBuilder();
        ret.append(name + " ");
        for (int i = 0; i < operands.size(); i++) {
            MCOperand o = operands.get(i);
            ret.append(o.prettyPrint());
            
            if (i != operands.size() - 1)
                ret.append(", ");
        }
        return ret.toString();
    }
    
    public final boolean isBranchingCode() {
        return isJump() || isRet();
    }
    
    public final boolean isJump() {
        return isCondJump() || isUncondJump();
    }
    
    /*
     * compiler needs to understand some machine code semantics
     */
    
    // a phi machine code will take several (label,value) pairs as operands
    public boolean isPhi() {
        return false;
    }
    
    // a jump machine code will take a label as operand
    public boolean isCondJump() {
        return false;
    }
    
    public boolean isUncondJump() {
        return false;
    }
    
    // a ret machine code will not take any operand
    public boolean isRet() {
        return false;
    }
}
