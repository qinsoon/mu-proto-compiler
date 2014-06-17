package uvm.runtime;

import java.util.List;

public class RuntimeFunction {
    public static final int CALL_CONV_C = 0;
    public static final int CALL_CONV_RJAVA = 1;
    
    public static final RuntimeFunction ALLOC_STACK = new RuntimeFunction(CALL_CONV_C, "allocateStack", null);
    
    int callConv;
    String funcName;
    List<uvm.Type> parameters;
    
    public RuntimeFunction(int cc, String funcName, List<uvm.Type> parameters) {
        this.callConv = cc;
        this.funcName = funcName;
        this.parameters = parameters;
    }
}
