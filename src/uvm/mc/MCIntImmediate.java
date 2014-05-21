package uvm.mc;

public class MCIntImmediate extends AbstractMCImmediate {
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
