package uvm;

public class IntImmediate extends ImmediateValue {
    long value;
    
    public IntImmediate(long value) {
        this.value = value;
        this.opcode = OpCode.INT_IMM;
    }
    
    @Override
    public String prettyPrint() {
        return "(INT_IMM " + value + ")";
    }
    
    public long getValue() {
        return value;
    }
}
