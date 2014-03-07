package uvm;

public class Label{
    String name;
    
    Instruction inst;
    
    public Label(String name) {
        this.name = name;
    }
    
    public String prettyPrint() {
        return "(LABEL " + name + ")";
    }
}
