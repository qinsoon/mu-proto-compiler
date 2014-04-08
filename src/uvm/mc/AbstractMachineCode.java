package uvm.mc;

import java.util.List;

public abstract class AbstractMachineCode {
    public int sequence;        // used for instruction numbering
    
    protected int node;
    
    protected String name;
    protected List<MCOperand> operands;
    
    protected uvm.mc.MCLabel label;
    
    protected uvm.mc.MCRegister reg;
    
    /**
     * this may not always be valid result (when the mc doesnt have a result)
     */
    public MCRegister getReg() {
        return reg;
    }
    
    public void setReg(MCRegister reg) {
        this.reg = reg;
    }
    
    public int getNumberOfOperands() {
        return operands.size();
    }
    
    public void setLabel(uvm.mc.MCLabel label) {
        this.label = label;
    }
    
    public MCLabel getLabel() {
        return label;
    }
    
    public MCOperand getOperand(int index) {
        return operands.get(index);
    }
    
    public void setOperand(int index, MCOperand operand) {
        operands.set(index, operand);
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
        if (reg != null)
            ret.append(" ->" + reg.prettyPrint());
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

    public int getNodeIndex() {
        return node;
    }

    public void setNodeIndex(int index) {
        this.node = index;
    }
}
