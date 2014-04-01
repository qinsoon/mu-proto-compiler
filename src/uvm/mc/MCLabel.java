package uvm.mc;

public class MCLabel extends MCOperand {
    String name;
    
    public MCLabel(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String prettyPrint() {
        return "#" + name;
    }
}
