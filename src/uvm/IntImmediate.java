package uvm;

public class IntImmediate extends ImmediateValue {
    long value;
    Type type;
    
    public IntImmediate(Type type, long value) {
        this.value = value;
        this.type = type;
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
