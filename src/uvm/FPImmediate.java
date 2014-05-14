package uvm;

public class FPImmediate extends ImmediateValue {
    double value;
    Type type;
    
    public FPImmediate(Type type, double value) {
        this.value = value;
        this.type = type;
        this.opcode = OpCode.FP_IMM;
    }
    
    @Override
    public String prettyPrint() {
        return "(FP_IMM " + value + ")";
    }

    public double getDouble() {
        return value;
    }
}
