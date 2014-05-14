package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstFOlt extends AbstractTypedBinop {

    public InstFOlt(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.FOLT;
    }

    @Override
    public String prettyPrint() {
        return "(FLOT " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
