package uvm.mc;

import uvm.IRTreeNode;

public abstract class MCOperand {
    public IRTreeNode highLevelOp;
    public abstract String prettyPrint();
    
    public String prettyPrintHLLOp() {
        if (highLevelOp == null)
            return "null";
        return highLevelOp.prettyPrint();
    }

	public String prettyPrintREPOnly() {
		return prettyPrint();
	}
}
