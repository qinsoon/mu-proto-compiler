package uvm;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    Label label;
    List<Instruction> insts;
    
    public BasicBlock(Function f, String name) {
        this.label = f.findOrCreateLabel(name);
        this.insts = new ArrayList<Instruction>();
    }
    
    public void addInstruction(Instruction i) {
        this.insts.add(i);
    }
    
    public void setInstructions(List<Instruction> insts) {
    	this.insts = insts;
    }
    
    public Label getLabel() {
        return label;
    }

    public String getName() {
        return label.name;
    }

    public List<Instruction> getInsts() {
        return insts;
    }
}
