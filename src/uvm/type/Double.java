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

}
