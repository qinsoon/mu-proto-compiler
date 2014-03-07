package uvm;

public class IntImmediate extends ImmediateValue {
    long value;
    
    public IntImmediate(long value) {
        this.value = value;
    }
    
    @Override
    public String prettyPrint() {
        return "(INT_IMM " + value + ")";
    }
}
