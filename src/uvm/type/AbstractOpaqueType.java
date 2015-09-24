package uvm.type;

import uvm.Type;

public abstract class AbstractOpaqueType extends Type {
	
	@Override
	public boolean isBaseRef() {
		return false;
	}
	
	@Override
	public boolean isIRef() {
		return true;
	}

}
