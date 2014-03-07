package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSlt extends InstTypedBinop {

    public InstSlt(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.SLT;
    }

    @Override
    public String prettyPrint() {
        return "(SLT " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
