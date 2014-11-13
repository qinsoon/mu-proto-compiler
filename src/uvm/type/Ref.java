package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class Ref extends AbstractPointerType {
	public static final Ref REF_VOID = findOrCreateRef(Void.T);
	
    Type referenced;
    
    public Type getReferenced() {
		return referenced;
	}

	public static Ref findOrCreateRef(Type referencedType) {
    	for (Type t : MicroVM.v.types.values()) {
    		if (t instanceof Ref && ((Ref) t).referenced.equals(referencedType))
    			return (Ref) t;
    	}
    	
    	Ref ret = new Ref(referencedType);
    	MicroVM.v.declareType(null, referencedType);
    	
    	return ret;
    }
    
    protected Ref(Type referenced) {
        super();
        this.referenced = referenced;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof Ref && ((Ref) o).referenced.equals(referenced))
    		return true;
    	return false;
    }

    @Override
    public String prettyPrint() {
        return "ref<" + referenced.prettyPrint() + ">";
    }
}
