package uvm.mc;

public class Label extends Operand {
    String name;
    
    public Label(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String prettyPrint() {
        return "#" + name;
    }
}
