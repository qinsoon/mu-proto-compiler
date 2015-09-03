package uvm.type;

import uvm.MicroVM;


public class Stack extends AbstractOpaqueType {
	public static final Stack T = new Stack();
	static {
		MicroVM.v.declareType(null, T);
	}
	
	public static final int MAX_STACK_SIZE_IN_BYTES = 4 << 20;	// 4Mb
																// if this is changed, change STACK_SIZE in runtime.h as well
	
	private Stack() {
		super();
	}

	@Override
	public String prettyPrint() {
		return "stack";
	}

	@Override
	public int size() {
		return MAX_STACK_SIZE_IN_BYTES * 8;
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
