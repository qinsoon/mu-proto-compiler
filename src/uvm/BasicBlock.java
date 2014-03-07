package uvm;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    String name;
    List<Instruction> insts;
    
    public BasicBlock(String name) {
        this.name = name;
        this.insts = new ArrayList<Instruction>();
    }
    
    public void addInstruction(Instruction i) {
        this.insts.add(i);
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInsts() {
        return insts;
    }
}
