package uvm.mc;

public class MCLabeledMemoryOperand extends MCMemoryOperand {
    public MCLabeledMemoryOperand(MCRegister base) {
		super(base);
	}

	public MCLabeledMemoryOperand() {
		super();
	}

	// displacement
    MCLabel dispLabel = null; // displacement represented by a label
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append(dispLabel.prettyPrint());
        if (base != null)
            ret.append("(" + base.prettyPrint() + ")");
        return ret.toString();
    }
    
    public MCLabel getDispLabel() {
        return dispLabel;
    }

    public void setDispLabel(MCLabel dispLabel) {
        this.dispLabel = dispLabel;
    }
}
