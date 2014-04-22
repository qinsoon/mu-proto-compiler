package uvm.type;

import uvm.Type;

public class Double extends Type {
    
    protected Double() {
        super();
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
        return 2;
    }

}
