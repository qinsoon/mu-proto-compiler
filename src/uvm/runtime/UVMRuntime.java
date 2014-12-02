package uvm.runtime;

public class UVMRuntime {
	public static final String LIB_NAME = "uvmrt.a";
	public static final String LIB_PATH = "runtime/" + LIB_NAME;
	
	public static final String INIT_FUNC = "_initRuntime";
	
	public static final String 	YIELDPOINT_PROTECT_AREA = "_yieldpoint_protect_page";
	public static final long 	YIELDPOINT_WRITE = 1L;
	
	public static final String 	YIELDPOINT_CHECK = "_yieldpoint_check";
	public static final long 	YIELDPOINT_ENABLE = 1;
	
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
