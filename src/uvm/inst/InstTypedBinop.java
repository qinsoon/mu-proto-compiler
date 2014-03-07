package uvm.inst;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.Type;
import uvm.Value;

public abstract class InstTypedBinop extends Instruction {
    Type type;
    Value op1;
    Value op2;
    
    public InstTypedBinop(Type type, Value op1, Value op2) {
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
        operands.add(op1);
        operands.add(op2);
    }
}
