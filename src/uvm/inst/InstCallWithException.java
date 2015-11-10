package uvm.inst;

import java.util.List;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.Label;
import uvm.OpCode;
import uvm.Value;

public class InstCallWithException extends AbstractUVMCall {
	Label normal;
	Label exception;
	
	// genmov phase may change the normal/excpetion edge (at MC level)
	// we need the information for dumping unwind table
	String actualNormal;
	String actualException;
	
	public InstCallWithException(Function callee, List<uvm.Value> arguments, Label normal, Label exception) {
		super(callee, arguments);
		this.normal = normal;
		this.exception = exception;

		this.opcode = OpCode.CALL_EXP;
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
	public boolean isBranching() {
		return true;
	}
	
	public String getActualNormal() {
		return actualNormal;
	}

	public void setActualNormal(String actualNormal) {
		this.actualNormal = actualNormal;
	}

	public String getActualException() {
		return actualException;
	}

	public void setActualException(String actualException) {
		this.actualException = actualException;
	}
}
