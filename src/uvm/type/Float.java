package uvm.type;

import uvm.Type;

public class Float extends Type {
    
    protected Float() {
        super();
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof Float)
    		return true;
    	return false;
    }

    @Override
    public int size() {
        return 32;
    }

    @Override
    public String prettyPrint() {
        return "float";
    }

    @Override
    public int fitsInGPR() {
        return 0;
    }

    @Override
    public int fitsInFPR() {
        return 1;
    }

	@Override
	public boolean isReference() {
		return false;
	}
}
