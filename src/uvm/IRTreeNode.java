package uvm;

import java.util.ArrayList;
import java.util.List;

import burm.BurmState;
import compiler.UVMCompiler;

public abstract class IRTreeNode {
    static int nextId = 0;
    int id;
    
    private static final int UNDEFINE = 0xABCD;
    
    protected List<IRTreeNode> children = new ArrayList<IRTreeNode>();
    protected int opcode = UNDEFINE;
    
    public BurmState state;
    
    protected IRTreeNode() {
        this.id = nextId;
        nextId++;
    }
    
    public int getArity() {
        return children.size();
    }
    
    public IRTreeNode getChild(int index) {
        return children.get(index);
    }
    
    public void addChild(IRTreeNode node) {
        children.add(node);
    }
    
    public int getOpcode() {
        return opcode;
    }
    
    public int getId() {
        return id;
    }
    
    public final String printNode() {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        ret.append(OpCode.getOpName(opcode));
        for (IRTreeNode c : children)
            ret.append(c.printNode());
        ret.append(')');
        return ret.toString();
    }
    
    public abstract String prettyPrint();
}
