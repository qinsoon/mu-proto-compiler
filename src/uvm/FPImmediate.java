package uvm;

public class FPImmediate extends ImmediateValue {
    double value;
    Type type;
    
    public FPImmediate(Type type, double value) {
        this.value = value;
        this.type = type;
        if (type.fitsInFPR() == 1)
            this.opcode = OpCode.FP_SP_IMM;
        else this.opcode = OpCode.FP_DP_IMM;
    }
    
    @Override
    public String prettyPrint() {
        return "(FP_IMM " + value + ")";
    }

    public double getDouble() {
        return value;
    }
    
    public float getFloat() {
        return (float) value;
    }
}
