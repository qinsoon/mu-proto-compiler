package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class Double extends Type {
    public static final Double DOUBLE = new Double();
    static {
    	MicroVM.v.declareType(null, DOUBLE);
    }
    
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

	@Override
	public boolean isReference() {
		return false;
	}
}
