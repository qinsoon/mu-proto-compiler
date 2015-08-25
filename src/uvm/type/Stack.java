package uvm.type;


public class Stack extends AbstractOpaqueType {
	public static final Stack T = new Stack();
	
	public static final int MAX_STACK_SIZE_IN_BYTES = 4 << 20;	// 4Mb

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
