package uvm.runtime;

import java.util.Arrays;
import java.util.List;

import uvm.FunctionSignature;
import uvm.Label;
import uvm.inst.InstCCall;
import uvm.type.Int;
import uvm.type.Ref;

public class RuntimeFunction {	
	public static final RuntimeFunction allocObj;
	public static final RuntimeFunction initObj;
	
	static {
		allocObj = new RuntimeFunction(InstCCall.CC_DEFAULT, 
				"_allocObj", 
				Ref.REF_VOID, 
				Arrays.asList(Int.I64, Int.I64));
		initObj  = new RuntimeFunction(InstCCall.CC_DEFAULT, 
				"_initObj",
				uvm.type.Void.T,
				Arrays.asList(Int.I64, Int.I64));
	}
    
    int callConv;
    String func;
    FunctionSignature sig;
    
    public RuntimeFunction(int cc, String func, uvm.Type returnType, List<uvm.Type> parameterTypes) {
        this.callConv = cc;
        this.func = func;
        this.sig = new FunctionSignature(returnType, parameterTypes);
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
