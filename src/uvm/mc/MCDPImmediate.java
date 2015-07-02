package uvm.mc;

public class MCDPImmediate extends AbstractMCImmediate {
	public static final MCDPImmediate ZERO = new MCDPImmediate(0);
	
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
