package uvm.inst;

import uvm.IRTreeNode;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstEq extends AbstractTypedBinop {

    public InstEq(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.EQ;
    }

    @Override
    public String prettyPrint() {
        return "(EQ " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
