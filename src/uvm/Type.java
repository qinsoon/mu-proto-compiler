package uvm;

import java.util.HashMap;

public abstract class Type {
    private static int typeCount = 0;
    
    int ID;

    protected Type() {
        this.ID = typeCount;
        typeCount++;
    }
    
    public abstract int size();
    
    public int getID() {
        return ID;
    }
    
    public abstract String prettyPrint();
    
    public abstract int fitsInGPR();
    public abstract int fitsInFPR();
}
