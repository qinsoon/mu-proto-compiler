package uvm.type;

import compiler.UVMCompiler;

import uvm.MicroVM;
import uvm.Type;

public class Int extends Type {
	public static final Int I1 = findOrCreate(1);
	public static final Int I64 = findOrCreate(64);
	
    int size;
    
    public static Int findOrCreate(int size) {
        for (Type t : MicroVM.v.getTypesMap().values()) {
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

    @Override
    public int fitsInGPR() {
        return (int) Math.ceil( ((double)size) / UVMCompiler.MC_REG_SIZE);
    }

    @Override
    public int fitsInFPR() {
        return 0;
    }

	@Override
	public boolean isBaseRef() {
		return false;
	}

	@Override
	public boolean isIRef() {
		return false;
	}

}
