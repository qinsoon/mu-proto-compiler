package uvm.mc;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMachineCode {
    public int sequence;        // used for instruction numbering
    
    protected int node;
    
    protected String name;
    protected List<MCOperand> operands;
    protected List<Boolean> opRegOnly;
    protected List<MCOperand> implicitUses = new ArrayList<MCOperand>();
    
    protected uvm.mc.MCLabel label;
    
    protected uvm.mc.MCOperand define;
    protected Boolean defineRegOnly;
    protected List<MCOperand> implicitDefines = new ArrayList<MCOperand>();
    
    protected uvm.IRTreeNode highLevelIR;
    
    public abstract String emit();
    
    public String getName() {
        return name;
    }
    
    public static void replaceMC(AbstractMachineCode oldMC, AbstractMachineCode newMC) {
        newMC.sequence = oldMC.sequence;
        newMC.node     = oldMC.node;        
        newMC.label    = oldMC.label;
    }
    
    public boolean isOpRegOnly(int i) {
    	return opRegOnly.get(i);
    }
    
    public boolean isDefineRegOnly() {
    	return defineRegOnly;
    }
    
    /**
     * this may not always be valid result (when the mc doesnt have a result)
     */
    public MCOperand getDefine() {
        return define;
    }
    
    public MCRegister getDefineAsReg() {
    	return (MCRegister) define;
    }
    
    public MCOperand getResultOp() {
        return define;
    }
    
    public void setDefine(MCOperand reg) {
        this.define = reg;
    }
    
    public int getNumberOfOperands() {
        return operands.size();
    }
    
    public int getNumberOfImplicitUses() {
        return implicitUses.size();
    }
    
    public MCOperand getImplicitUse(int index) {
        return implicitUses.get(index);
    }
    
    public void addImplicitUse(MCOperand op) {
        implicitUses.add(op);
    }
    
    public int getNumberOfImplicitDefines() {
        return implicitDefines.size();
    }
    
    public MCOperand getImplicitDefine(int index) {
        return implicitDefines.get(index);
    }
    
    public void addImplicitDefine(MCOperand op) {
        implicitDefines.add(op);        
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
    
    public String prettyPrintOneline() {
        StringBuilder ret = new StringBuilder();
        if (label != null)
            ret.append("#" + label.name + ":");
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
        if (define != null)
            ret.append(" -> " + define.prettyPrint());

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
    
    public boolean isCall() {
        return false;
    }
    
    // we will try register coalescing for mov mc
    public boolean isMov() {
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

    public uvm.IRTreeNode getHighLevelIR() {
        return highLevelIR;
    }

    public void setHighLevelIR(uvm.IRTreeNode highLevelIR) {
        this.highLevelIR = highLevelIR;
    }
}
