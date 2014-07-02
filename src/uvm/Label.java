package uvm;

public class Label extends Value {
    String name;
    
    Label(String name) {
        this.name = name;
        this.opcode = OpCode.LABEL;
    }
    
    public String getName() {
        return name;
    }
    
    public String prettyPrint() {
        return "(LABEL " + name + ")";
    }

    @Override
    public boolean isRegister() {
        return false;
    }
}
