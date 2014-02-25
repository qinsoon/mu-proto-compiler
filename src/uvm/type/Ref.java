package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class Ref extends Type {
    Type referenced;
    
    protected Ref(Type referenced) {
        super();
        this.referenced = referenced;
    }
    
    @Override
    public int size() {
        return MicroVM.POINTER_SIZE;
    }
}
