package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class IRef extends Type {
    Type referenced;
    
    protected IRef(Type referenced) {
        super();
        this.referenced = referenced;
    }
    
    @Override
    public int size() {
        // actual pointer + base pointer
        return MicroVM.POINTER_SIZE * 2;
    }

    @Override
    public String prettyPrint() {
        return "iref<" + referenced.prettyPrint() + ">";
    }

    @Override
    public int fitsInGPR() {
        return 1;
    }

    @Override
    public int fitsInFPR() {
        return 0;
    }

}
