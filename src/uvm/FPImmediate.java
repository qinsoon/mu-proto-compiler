package uvm;

public class FPImmediate extends ImmediateValue {
    double value;
    Type type;
    
    public FPImmediate(Type type, double value) {
        this.value = value;
        this.type = type;
        this.opcode = OpCode.FP_DP_IMM;
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
    
    @Override
    public boolean equals(Object o) {
    	if (o == null)
    		return false;
    	
    	if (o instanceof FPImmediate && ((FPImmediate) o).value == value && ((FPImmediate) o).type.equals(type))
    		return true;    	
    	else return false;
    }
}
