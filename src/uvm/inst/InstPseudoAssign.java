package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Register;

public class InstPseudoAssign extends Instruction {
    public Instruction rhs;
    public Register lhs;
    
    public InstPseudoAssign(Register lhs, Instruction rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.opcode = OpCode.PSEUDO_ASSIGN;
    }
    
    @Override
    public String prettyPrint() {
        return "(ASSIGN " + lhs.prettyPrint() + " " + rhs.prettyPrint() + ")";
    }

}
