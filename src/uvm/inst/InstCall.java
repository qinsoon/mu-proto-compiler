package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.OpCode;

public class InstCall extends AbstractCall {
    Function callee;    
    
    public InstCall(Function callee, List<uvm.Value> arguments) {
    	super(callee.getName(), arguments);
        this.callee = callee;
        this.operands.add(callee.getFuncLabel());
        this.operands.addAll(arguments);
        this.opcode = OpCode.CALL;
    }
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("CALL " + callee.getName() + "(");
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
    
    @Override
    public FunctionSignature getSig() {
    	return callee.getSig();
    }
}
