package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstShl extends AbstractTypedBinop {

    public InstShl(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.SHL;
    }

    @Override
    public String prettyPrint() {
        return "(SHL " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
