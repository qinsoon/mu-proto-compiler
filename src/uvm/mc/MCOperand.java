package uvm.mc;

import uvm.IRTreeNode;

public abstract class MCOperand {
    public IRTreeNode highLevelOp;
    public abstract String prettyPrint();
}
