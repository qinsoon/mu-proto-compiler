package uvm.type;

import uvm.MicroVM;
import uvm.Type;

public abstract class AbstractPointerType extends Type {

	@Override
	public final int size() {
		return MicroVM.POINTER_SIZE;
	}

	@Override
	public final int fitsInGPR() {
		return 1;
	}

	@Override
	public final int fitsInFPR() {
		return 0;
	}

}
