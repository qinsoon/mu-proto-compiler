package uvm.mc;

public class IntImmediate extends Operand {
    long value;
    
    public IntImmediate(long value) {
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
