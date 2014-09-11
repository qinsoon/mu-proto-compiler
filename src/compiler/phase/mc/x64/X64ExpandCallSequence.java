package compiler.phase.mc.x64;

import java.util.ArrayList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.Function;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64ExpandCallSequence extends AbstractMCCompilationPhase {

    public X64ExpandCallSequence(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        X64CallConvention cc = new X64CallConvention();
        
        cc.genPrologue(cf);
        cc.genEpilogue(cf);
        
        for (MCBasicBlock bb : cf.BBs) {
            List<AbstractMachineCode> newMC = new ArrayList<AbstractMachineCode>();
            
            for (AbstractMachineCode mc : bb.getMC()) {    
                if (mc.isCall()) {
                    // use this instance for this call only
                    X64CallConvention cc1 = new X64CallConvention();
                    
                    // expand calling sequence
                    MCLabel label = (MCLabel) mc.getOperand(0);
                    Function callee = MicroVM.v.getFunction(label.getName());
                    newMC.addAll(cc1.callerSetupCallSequence(cf, callee, mc));
                    newMC.addAll(cc1.callerCleanupCallSequence(cf, callee, mc));
                } else {
                    newMC.add(mc);
                }
            }
            
            bb.setMC(newMC);
        }
    }

}
