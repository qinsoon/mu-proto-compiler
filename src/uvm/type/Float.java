package uvm.type;

import uvm.Type;

public class Float extends Type {
    
    protected Float() {
        super();
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

}
