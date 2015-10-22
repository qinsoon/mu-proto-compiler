package uvm.inst;

import java.util.List;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.Label;
import uvm.OpCode;
import uvm.Value;

public class InstCallWithException extends AbstractCall {
	Function callee;
	
	Label normal;
	Label exception;
	
	public InstCallWithException(Function callee, List<uvm.Value> arguments, Label normal, Label exception) {
		super(callee.getName(), arguments);
		this.callee = callee;
		this.normal = normal;
		this.exception = exception;
//		this.operands.add(normal);
//		this.operands.add(exception);
		this.opcode = OpCode.CALL_EXP;
	}
	
	public Function getCallee() {
		return callee;
	}
	
	public Label getNormalLabel() {
		return normal;				
	}
	
	public Label getExceptionLabel() {
		return exception;
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
        ret.append(" normal: " + normal.prettyPrint());
        ret.append(", exception: " + exception.prettyPrint());
        return ret.toString();
    }
    
	@Override
	public FunctionSignature getSig() {
		return callee.getSig();
	}
	
	@Override
	public boolean isBranching() {
		return true;
	}
}
