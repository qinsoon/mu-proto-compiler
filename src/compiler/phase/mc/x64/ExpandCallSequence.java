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

public class ExpandCallSequence extends AbstractMCCompilationPhase {

    public ExpandCallSequence(String name) {
        super(name);
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
                    // expand calling sequence
                    MCLabel label = (MCLabel) mc.getOperand(0);
                    Function callee = MicroVM.v.getFunction(label.getName());
                    List<AbstractMachineCode> expandedCallSequence = cc.callerSetupCallSequence(cf, callee, mc);
                    
                    newMC.addAll(expandedCallSequence);
                } else {
                    newMC.add(mc);
                }
            }
            
            bb.setMC(newMC);
        }
    }

}
