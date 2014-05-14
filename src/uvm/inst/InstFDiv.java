package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstFDiv extends AbstractTypedBinop {

    public InstFDiv(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.FDIV;
    }

    @Override
    public String prettyPrint() {
        return "(FDIV " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
