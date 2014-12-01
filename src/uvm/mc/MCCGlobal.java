package uvm.mc;

public class MCCGlobal extends MCOperand {
	String name;
	
	public MCCGlobal(String name) {
		this.name = name;
	}
	
	@Override
	public String prettyPrint() {
		return name;
	}

	public String getName() {
		return name;
	}

}
