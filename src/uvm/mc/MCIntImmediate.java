package uvm.mc;

public class MCIntImmediate extends AbstractMCImmediate {
	public static final MCIntImmediate ZERO = new MCIntImmediate(0L);
	
    long value;
    
    public MCIntImmediate(long value) {
        this.value = value;
    }
    
    public long getValue() {
        return value;
    }
    
    @Override
    public String prettyPrint() {
        return "$" + Long.toString(value) + "(0x" + Long.toHexString(value) + ")";
    }
}
