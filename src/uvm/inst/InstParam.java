package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;

public class InstParam extends Instruction {
    int paramIndex;
    
    public InstParam(int index, uvm.Type type) {
        this.paramIndex = index;
        if (type instanceof uvm.type.Double)
        	opcode = OpCode.PARAM_DP;
        else if (type instanceof uvm.type.Float)
        	opcode = OpCode.PARAM_SP;
        else opcode = OpCode.PARAM_GPR;
    }

    @Override
    public String prettyPrint() {
        return "(PARAM " + paramIndex + ")";
    }
    
    public int getIndex() {
        return paramIndex;
    }
}
