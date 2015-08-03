package uvm.mc;

public class MCIndexedDispMemoryOperand extends MCDispMemoryOperand {
    // index register
    MCRegister index = null;
    // scale value (log power of 2)
    int scale = 0;        

    public MCIndexedDispMemoryOperand(MCRegister base, int disp, MCRegister index, int scale) {
    	super(base, disp);
    	this.index = index;
    	this.scale = scale;
    }
    
    public MCIndexedDispMemoryOperand(MCRegister base, int disp, MCRegister index, MCOperand scale) {
    	super(base, disp);
    	this.index = index;
    	this.scale = (int) ((MCIntImmediate)scale).value;
    }
    
    public MCIndexedDispMemoryOperand(MCRegister base, MCOperand disp, MCRegister index, int scale) {
    	super(base, disp);
    	this.index = index;
    	this.scale = scale;
    }
    
    
    public MCIndexedDispMemoryOperand(MCRegister base, MCOperand disp, MCRegister index, MCOperand scale) {
    	super(base, disp);
    	this.index = index;
    	this.scale = (int) ((MCIntImmediate)scale).value;
    }
    
    public MCRegister getIndex() {
        return index;
    }

    public void setIndex(MCRegister index) {
        this.index = index;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
    
    @Override
    public String prettyPrint() {
    	StringBuilder ret = new StringBuilder();
    	
    	ret.append(disp + "(" + base.prettyPrint() + "+");
    	if (index != null) 
    		ret.append(index.prettyPrint() + "*" + scale);
    	ret.append(")");
    	
    	return ret.toString();
    }
}
