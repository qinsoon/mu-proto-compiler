package uvm.mc;

public class MCLabel extends MCOperand {
    String name;
    
    public MCLabel(String name) {
        this.name = name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof MCLabel && ((MCLabel)o).name.equals(name))
            return true;
        
        return false;
    }
    
    public String getName() {
        return name;
    }
    
    public String prettyPrint() {
        return "#" + name;
    }

	@Override
	public String prettyPrintREPOnly() {
		return prettyPrint();
	}
}
