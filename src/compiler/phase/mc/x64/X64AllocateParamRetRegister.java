package compiler.phase.mc.x64;

import uvm.CompiledFunction;

import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64AllocateParamRetRegister extends AbstractMCCompilationPhase{

    public X64AllocateParamRetRegister(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
    	verboseln("alloc param/ret register for " + cf.getOriginFunction().getName());
        X64UVMCallConvention cc = new X64UVMCallConvention();
        cc.calleeInitParameterRegisters(cf);
        cc.calleeInitReturnRegister(cf);
    }

}
