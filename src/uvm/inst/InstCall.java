package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.Instruction;
import uvm.OpCode;

public class InstCall extends Instruction {
    Function callee;
    List<uvm.Value> arguments;
    
    public InstCall(Function callee, List<uvm.Value> arguments) {
        this.callee = callee;
        this.arguments = arguments;
        this.operands.add(callee.getFuncLabel());
        this.operands.addAll(arguments);
        this.opcode = OpCode.CALL;
    }
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("CALL(");
        for (uvm.Value v : arguments) {
            ret.append(v.prettyPrint());
            ret.append(',');
        }
        ret.append(')');
        return ret.toString();
    }

    public Function getCallee() {
        return callee;
    }

    public List<uvm.Value> getArguments() {
        return arguments;
    }

}
