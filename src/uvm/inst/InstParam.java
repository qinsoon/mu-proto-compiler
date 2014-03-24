package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;

public class InstParam extends Instruction {
    int paramIndex;
    
    public InstParam(int index) {
        this.paramIndex = index;
        opcode = OpCode.PARAM;
    }

    @Override
    public String prettyPrint() {
        return "(PARAM " + paramIndex + ")";
    }
    
    public int getIndex() {
        return paramIndex;
    }
}
