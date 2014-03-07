package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Value;

public class InstRet2 extends Instruction {
    Value val;
    
    public InstRet2(Value val) {
        this.val = val;
        operands.add(val);
        opcode = OpCode.RET2;
    }

    @Override
    public String prettyPrint() {
        return "(RET2 " + val.prettyPrint() + ")";
    }
}
