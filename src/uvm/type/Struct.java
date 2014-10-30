package uvm.type;

import java.util.List;

import uvm.MicroVM;
import uvm.Type;

public class Struct extends Type {
    List<Type> types;
    List<Integer> offsets;
    int size = 0;
    
    protected Struct(List<Type> types) {
        super();
        this.types = types;
        
        MicroVM.v.objectModel.layoutStruct(this);
    }

	public static Struct findOrCreateStruct(List<Type> types) {
		for (Type t : MicroVM.v.types.values()) {
			if (t instanceof Struct && isTypeListEqual(types, ((Struct) t).types))
				return (Struct) t;
		}
			
		Struct ret = new Struct(types);
		MicroVM.v.declareType(null, ret);

		return ret;
	}
    
    public Type getType(int i) {
    	return types.get(i);
    }
    
    public List<Type> getTypes() {
    	return types;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof Struct) {
    		// fast check
    		if (((Struct) o).size != size)
    			return false;
    		
    		return isTypeListEqual(((Struct) o).types, types);
    	}
    	
    	return false;
    }
    
    private static boolean isTypeListEqual(List<Type> types1, List<Type> types2) {
    	if (types1.size() != types2.size())
    		return false;
    	
    	for (int i = 0; i < types1.size(); i++)
    		if (!types1.get(i).equals(types2.get(i)))
    			return false;

    	return true;
    }

    @Override
    public int size() {
        return size;
    }
    
    public int getOffset(int i) {
    	return offsets.get(i);
    }
    
	public List<Integer> getOffsets() {
		return offsets;
	}

	public void setOffsets(List<Integer> offsets) {
		this.offsets = offsets;
	}

	public void setSize(int size) {
		this.size = size;
	}

    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("struct<");
        for (int i = 0; i < types.size(); i++) {
            ret.append(types.get(i).prettyPrint());
            if (i != types.size() - 1)
                ret.append(" ");
        }
        ret.append(">");
        return ret.toString();
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
