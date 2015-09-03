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
    	for (Type t : MicroVM.v.getTypesMap().values()) {
    		if (t instanceof Ref && ((Ref) t).referenced.equals(referencedType))
    			return (Ref) t;
    	}
    	
    	Ref ret = new Ref(referencedType);
    	MicroVM.v.declareType(null, ret);
    	
    	return ret;
    }
    
    protected Ref(Type referenced) {
        super();
        this.referenced = referenced;
    }
    


    @Override
    public String prettyPrint() {
        return "ref<" + referenced.prettyPrint() + ">";
    }

	@Override
	public boolean isReference() {
		return true;
	}
}
