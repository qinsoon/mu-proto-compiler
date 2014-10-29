package uvm;

import java.util.HashMap;

public abstract class Type {
    private static int typeCount = 0;
    
    int ID;

    protected Type() {
        this.ID = typeCount;
        typeCount++;
    }
    
    /**
     * bits 
     * @return
     */
    public abstract int size();
    
    /**
     * sizeof in bytes
     */
    public final int sizeInBytes() {
    	return size() % 8 == 0 ? size() / 8 : size() / 8 + 1;
    }
    
    /**
     * alignment in bytes
     * @return
     */
    public abstract int alignmentInBytes();
    
    public int getID() {
        return ID;
    }
    
    public abstract String prettyPrint();
    
    public abstract int fitsInGPR();
    public abstract int fitsInFPR();
}
