package uvm.type;

import uvm.MicroVM;

public class Thread extends AbstractOpaqueType {
	public static final Thread T = new Thread();

	@Override
	public String prettyPrint() {
		return "thread";
	}

	@Override
	public int size() {
		return MicroVM.POINTER_SIZE;
	}

	@Override
	public int fitsInGPR() {
		return 1;
	}

	@Override
	public int fitsInFPR() {
		return 0;
	}

}
