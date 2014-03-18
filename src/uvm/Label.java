package uvm;

public class Label extends IRTreeNode {
    String name;
    
    Instruction inst;
    
    Label(String name) {
        this.name = name;
        this.opcode = OpCode.LABEL;
    }
    
    public String prettyPrint() {
        return "(LABEL " + name + ")";
    }
}
