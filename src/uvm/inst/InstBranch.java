package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.Register;

public class InstBranch extends Instruction {
    Label target;
    
    public InstBranch(Label target) {
        this.target = target;
        opcode = OpCode.BRANCH;
    }

    @Override
    public String prettyPrint() {
        return "(BRANCH " + target.prettyPrint() + ")";
    }
    
    public Label getTarget() {
        return target;
    }
    
    @Override
    public boolean isBranching() {
    	return true;
    }
}
