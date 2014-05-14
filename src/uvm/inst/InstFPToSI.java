package uvm.inst;

import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstFPToSI extends AbstractTypeConversion {

    public InstFPToSI(Type from, Type to, Value op) {
        super(from, to, op);
        opcode = OpCode.FPTOSI;
    }

    @Override
    public String prettyPrint() {
        return "(FPTOSI " + op.prettyPrint() + ")";
    }

}
