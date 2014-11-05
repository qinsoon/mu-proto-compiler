package uvm.runtime;

public class UVMRuntime {
	public static final String LIB_NAME = "uvmrt.a";
	public static final String LIB_PATH = "runtime/" + LIB_NAME;
	
	public static final String INIT_FUNC = "_initRuntime";
	
	boolean heapUsed;
	boolean exceptionUsed;
	
	public UVMRuntime() {
		heapUsed = true;
		exceptionUsed = false;
	}
	
	public boolean needToInitRuntime() {
		return heapUsed || exceptionUsed;
	}
	
	public boolean isHeapUsed() {
		return heapUsed;
	}
}
