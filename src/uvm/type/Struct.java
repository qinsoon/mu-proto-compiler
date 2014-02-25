package uvm.type;

import java.util.List;

import uvm.Type;

public class Struct extends Type {
    List<Type> types;
    int size = 0;
    
    protected Struct(List<Type> types) {
        super();
        this.types = types;
        // FIXME: need to care about alignment
        for (Type t : types)
            size += t.size();
    }

    @Override
    public int size() {
        return size;
    }

}
