package compiler.phase.mc.x64;

import uvm.CompiledFunction;

import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64AllocateParamRetRegister extends AbstractMCCompilationPhase{

    public X64AllocateParamRetRegister(String name, boolean verbose) {
        super(name, verbose);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        X64CallConvention cc = new X64CallConvention();
        cc.calleeInitParameterRegisters(cf);
        cc.calleeInitReturnRegister(cf);
    }

}
