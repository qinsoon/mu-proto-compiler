package uvm.mc;

public class MCSPImmediate extends AbstractMCImmediate {
    float value;
    MCMemoryOperand inMem;

    public MCSPImmediate(float value) {
        this.value = value;
    }
    
    public float getValue() {
        return value;
    }
    
    @Override
    public String prettyPrint() {
        return "$" + Float.toString(value);
    }
}
