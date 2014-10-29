package uvm.mc;

public class MCDispMemoryOperand extends MCMemoryOperand {
	int disp = 0;
	
    public MCDispMemoryOperand(MCRegister base, int disp) {
		super(base);
		this.disp = disp;
	}
    
    public MCDispMemoryOperand(MCRegister base) {
    	super(base);
    }
    
    public MCDispMemoryOperand() {
    	super();
    }
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (base != null)
            ret.append("[" + base.prettyPrint() + "]+");
        if (index != null)
            ret.append("[" + index.prettyPrint() + "]*");
        ret.append("(2^" + scale + ")+" + disp);
        return ret.toString();
    }
    
    public int getDisp() {
        return disp;
    }

    public void setDisp(int disp) {
        this.disp = disp;
    }

}
