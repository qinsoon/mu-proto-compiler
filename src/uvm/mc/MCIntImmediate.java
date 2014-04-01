package uvm.mc;

public class MCIntImmediate extends MCOperand {
    long value;
    
    public MCIntImmediate(long value) {
        this.value = value;
    }
    
    public long getValue() {
        return value;
    }
    
    @Override
    public String prettyPrint() {
        return "$" + Long.toString(value);
    }
}
