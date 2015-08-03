package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;

public class InstRet extends Instruction {
    Value val;
    
    public InstRet(Value val) {
        this.val = val;
        operands.add(val);
        opcode = OpCode.RET;
    }
    
    public Value getVal() {
    	return val;
    }

    @Override
    public String prettyPrint() {
        return "(RET2 " + val.prettyPrint() + ")";
    }
}
