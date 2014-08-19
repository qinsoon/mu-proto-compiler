package uvm.mc.linearscan;

import uvm.mc.AbstractMachineCode;

public class Position {
    public static final int DEFINE  = 0;
    public static final int USE     = 1;
    
    int type;
    int index;
    
    AbstractMachineCode inst;
    int operandIndex;
    boolean regOnly;
    
    public Position(int index, int type, AbstractMachineCode inst, int operandIndex, boolean regOnly) {
        this.type = type;
        this.index = index;
        this.inst = inst;
        this.operandIndex = operandIndex;
        this.regOnly = regOnly;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Position && ((Position) o).index == this.index && ((Position) o).type == this.type)
            return true;
                    
        return false;
    }
    
    public String prettyPrint() {
        if (type == USE)
            return "USE at " + index;
        else return "DEFINE at " + index;
    }
    
    public boolean isDefine() {
        return this.type == DEFINE;
    }
    
    public boolean isUse() {
        return this.type == USE;
    }
    
    public int getIndex() {
        return index;
    }
}
