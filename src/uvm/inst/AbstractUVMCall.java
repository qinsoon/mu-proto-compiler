package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.FunctionSignature;
import uvm.OpCode;

public abstract class AbstractUVMCall extends AbstractCall {
    Function callee;    
    
    public AbstractUVMCall(Function callee, List<uvm.Value> arguments) {
    	super(callee.getName(), arguments);
        this.callee = callee;
    }

    public Function getCallee() {
        return callee;
    }
    
    @Override
    public FunctionSignature getSig() {
    	return callee.getSig();
    }
}
