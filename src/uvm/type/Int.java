package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class Int extends Type {
    int size;
    
    public static Int findOrCreate(int size) {
        for (Type t : MicroVM.v.types.values()) {
            if (t instanceof Int && ((Int)t).size == size)
                return (Int)t;
        }
        
        Int ret = new Int(size);
        MicroVM.v.declareType(null, ret);
        
        return ret;
    }
    
    protected Int(int size) {
        super();
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Int))
            return false;
        
        return this.size == ((Int)o).size;
    }
    
    @Override
    public String toString() {
        return "int<" + size + ">";
    }

    @Override
    public String prettyPrint() {
        return toString();
    }
}
