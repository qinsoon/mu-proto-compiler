package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSRem extends AbstractTypedBinop {

    public InstSRem(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.SREM;
    }

    @Override
    public String prettyPrint() {
        return "(SREM " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }
    
}
