package uvm.type;

import uvm.Type;

public class Void extends Type {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String prettyPrint() {
        return "";
    }

    @Override
    public int fitsInGPR() {
        return 0;
    }

    @Override
    public int fitsInFPR() {
        return 0;
    }
}
