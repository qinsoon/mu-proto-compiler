package uvm.mc;

public class MCDPImmediate extends MCOperand {
    double value;
    
    public MCDPImmediate(double value) {
        this.value = value;
    }
    
    public double getValue() {
        return value;
    }

    @Override
    public String prettyPrint() {
        return "$" + Double.toString(value);
    }

}
