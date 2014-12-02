package uvm.runtime;

import java.util.Arrays;
import java.util.List;

import uvm.FunctionSignature;
import uvm.Label;
import uvm.inst.InstCCall;
import uvm.mc.MCLabel;
import uvm.type.Int;
import uvm.type.Ref;

public class RuntimeFunction {	
	public static final RuntimeFunction allocObj;
	public static final RuntimeFunction initObj;
	public static final RuntimeFunction yieldpoint;
	
	static {
		allocObj = new RuntimeFunction(InstCCall.CC_DEFAULT, 
				"_allocObj", 
				Ref.REF_VOID, 
				Arrays.asList(Int.I64, Int.I64));
		initObj  = new RuntimeFunction(InstCCall.CC_DEFAULT, 
				"_initObj",
				uvm.type.Void.T,
				Arrays.asList(Int.I64, Int.I64));
		yieldpoint = new RuntimeFunction(InstCCall.CC_DEFAULT, 
				"_yieldpoint",
				uvm.type.Void.T,
				Arrays.asList(uvm.type.Void.T));
	}
    
    int callConv;
    String func;
    MCLabel label;
    FunctionSignature sig;
    
    public RuntimeFunction(int cc, String func, uvm.Type returnType, List<uvm.Type> parameterTypes) {
        this.callConv = cc;
        this.func = func;
        this.sig = new FunctionSignature(returnType, parameterTypes);
    }
    
    public MCLabel getLabel() {
    	if (label == null)
    		label = new MCLabel(func);
    	
    	return label;
    }

	public int getCallConv() {
		return callConv;
	}

	public String getFuncName() {
		return func;
	}

	public FunctionSignature getFunctionSignature() {
		return sig;
	}
}
