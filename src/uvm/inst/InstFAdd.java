package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstFAdd extends AbstractTypedBinop {

    public InstFAdd(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.FADD;
    }

    @Override
    public String prettyPrint() {
        return "(FADD " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
