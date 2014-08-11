package uvm.mc;

public class MCLabeledMemoryOperand extends MCMemoryOperand {
    // displacement
    MCLabel dispLabel = null; // displacement represented by a label
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (base != null)
            ret.append("[" + base.prettyPrint() + "]+");
        if (index != null)
            ret.append("[" + index.prettyPrint() + "]*");
        ret.append("(2^" + scale + ")+" + dispLabel.prettyPrint());
        return ret.toString();
    }
    
    public MCLabel getDispLabel() {
        return dispLabel;
    }

    public void setDispLabel(MCLabel dispLabel) {
        this.dispLabel = dispLabel;
    }
}
