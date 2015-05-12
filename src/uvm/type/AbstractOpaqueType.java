package uvm.type;

import uvm.Type;

public abstract class AbstractOpaqueType extends Type {

	@Override
	public final boolean isReference() {
		return false;
	}

}
