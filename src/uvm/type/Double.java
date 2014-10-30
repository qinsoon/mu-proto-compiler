package uvm.type;

import uvm.Type;

public class Double extends Type {
    public static final Double DOUBLE = new Double();
    
    protected Double() {
        super();
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof Double)
    		return true;
    	else return false;
    }

    @Override
    public int size() {
        return 64;
    }

    @Override
    public String prettyPrint() {
        return "double";
    }

    @Override
    public int fitsInGPR() {
        return 0;
    }

    @Override
    public int fitsInFPR() {
        return 1;
    }
}
