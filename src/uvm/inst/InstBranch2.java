package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.Value;

public class InstBranch2 extends Instruction {
    Value cond;
    Label ifTrue;
    Label ifFalse;
    
    public InstBranch2(Value cond, Label ifTrue, Label ifFalse) {
        this.cond = cond;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        operands.add(cond);
        opcode = OpCode.BRANCH2;
    }

    @Override
    public String prettyPrint() {
        return "(BRANCH2 " + cond.prettyPrint() + " " + ifTrue.prettyPrint() + " " + ifFalse.prettyPrint() + ")";
    }
    
    public Value getCond() {
        return cond;
    }
    
    public Label getIfTrue() {
        return ifTrue;
    }
    
    public Label getIfFalse() {
        return ifFalse;
    }
    
    @Override
    public boolean isBranching() {
    	return true;
    }
}
