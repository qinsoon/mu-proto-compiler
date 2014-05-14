package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstSIToFP extends AbstractTypeConversion {

    public InstSIToFP(Type from, Type to, Value op) {
        super(from, to, op);
        opcode = OpCode.SITOFP;
    }
    
    @Override
    public String prettyPrint() {
        return "(SITOFP " + op.prettyPrint() + ")";
    }

}
