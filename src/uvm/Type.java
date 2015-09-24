package uvm;

import java.util.HashMap;

public abstract class Type {
    private static int typeCount = 0;
    public static int getTypeCount() {
    	return typeCount;
    }
    
    int ID;

    protected Type() {
        this.ID = typeCount;
        System.out.println("Created type: ID=" + ID + ", class:" + this.getClass().getName());
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
    
    int align = -1;
    /**
     * alignment in bytes
     * @return
     */
    public final int alignmentInBytes() {
    	if (align == -1)
    		align = MicroVM.v.objectModel.getAlignment(this);
    	
    	return align;
    }
    
    public int getID() {
        return ID;
    }
    
    public final boolean isReference() {
    	return isBaseRef() || isIRef();
    }
    public abstract boolean isBaseRef();
    public abstract boolean isIRef();
    
    public abstract String prettyPrint();
    
    public abstract int fitsInGPR();
    public abstract int fitsInFPR();
}
