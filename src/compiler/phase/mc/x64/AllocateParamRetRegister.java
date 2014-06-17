package compiler.phase.mc.x64;

import uvm.CompiledFunction;

import compiler.phase.mc.AbstractMCCompilationPhase;

public class AllocateParamRetRegister extends AbstractMCCompilationPhase{

    public AllocateParamRetRegister(String name) {
        super(name);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        X64CallConvention cc = new X64CallConvention();
        cc.calleeInitParameterRegisters(cf);
        cc.calleeInitReturnRegister(cf);
    }

}
