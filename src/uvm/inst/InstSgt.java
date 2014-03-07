package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSgt extends InstTypedBinop {

    public InstSgt(Type type, Value op1, Value op2) {
        super(type, op1, op2);
        opcode = OpCode.SGT;
    }

    @Override
    public String prettyPrint() {
        return "(SGT " + op1.prettyPrint() + " " + op2.prettyPrint() + ")";
    }

}
