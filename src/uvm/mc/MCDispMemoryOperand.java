package uvm.mc;

public class MCDispMemoryOperand extends MCMemoryOperand {
	int disp = 0;
	
    public MCDispMemoryOperand(MCRegister base, int disp) {
		super(base);
		this.disp = disp;
	}
    
    public MCDispMemoryOperand(MCRegister base, MCOperand disp) {
    	super(base);
    	this.disp = (int) ((MCIntImmediate)disp).value;
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
        ret.append(disp + "(" + base.prettyPrint() + ")");
        return ret.toString();
    }
    
    public int getDisp() {
        return disp;
    }

    public void setDisp(int disp) {
        this.disp = disp;
    }
    
    public MCDispMemoryOperand cloneWithDisp(int newDisp) {
    	return new MCDispMemoryOperand(base, newDisp);
    }

}
