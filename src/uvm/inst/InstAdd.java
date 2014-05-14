package uvm.inst;

import uvm.IRTreeNode;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstAdd extends AbstractTypedBinop {

    public InstAdd(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.ADD;
    }

    @Override
    public String prettyPrint() {
        return "(ADD " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
