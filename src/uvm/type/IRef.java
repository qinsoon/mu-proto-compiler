package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public class IRef extends AbstractPointerType {
	public static final IRef IREF_VOID = findOrCreateIRef(Void.T);
	
    Type referenced;
    
    public static IRef findOrCreateIRef(Type referencedType) {
    	for (Type t : MicroVM.v.getTypesMap().values()) {
    		if (t instanceof IRef && ((IRef) t).referenced.equals(referencedType))
    			return (IRef) t;
    	}
    	
    	IRef ret = new IRef(referencedType);
    	MicroVM.v.declareType(null, ret);
    	
    	return ret;
    }
    
    protected IRef(Type referenced) {
        super();
        this.referenced = referenced;
    }
    
    @Override
    public String prettyPrint() {
        return "iref<" + referenced.prettyPrint() + ">";
    }

	public Type getReferenced() {
		return referenced;
	}

	@Override
	public boolean isBaseRef() {
		return false;
	}

	@Override
	public boolean isIRef() {
		return true;
	}

}
