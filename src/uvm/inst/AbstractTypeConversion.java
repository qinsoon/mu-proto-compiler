package uvm.inst;

import uvm.Instruction;
import uvm.Type;
import uvm.Value;

public abstract class AbstractTypeConversion extends Instruction {
    Type fromType;
    Type toType;
    Value op;
    
    public AbstractTypeConversion(Type from, Type to, Value op) {
        this.fromType = from;
        this.toType = to;
        this.op = op;
        operands.add(op);
    }
}
