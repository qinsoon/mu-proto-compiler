package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstPhi extends Instruction {
    Type type;
    
    Value val1;
    Label label1;
    Value val2;
    Label label2;
    
    public InstPhi(Type type, Value val1, Label label1, Value val2, Label label2) {
        this.val1 = val1;
        this.label1 = label1;
        this.val2 = val2;
        this.label2 = label2;
        
        operands.add(val1);
        operands.add(val2);
        
        opcode = OpCode.PHI;
    }

    @Override
    public String prettyPrint() {
        return "(PHI " + val1.prettyPrint() + " " + label1.prettyPrint() + " " + val2.prettyPrint() + " " + label2.prettyPrint() + ")";
    }
}
