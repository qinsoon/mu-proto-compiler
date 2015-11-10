package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.FunctionSignature;
import uvm.Instruction;
import uvm.OpCode;

public class InstCall extends AbstractUVMCall {
    Function callee;    
    
    public InstCall(Function callee, List<uvm.Value> arguments) {
    	super(callee, arguments);

        this.opcode = OpCode.CALL;
    }
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("CALL " + callee.getName() + "(");
        for (uvm.Value v : getArguments()) {
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
