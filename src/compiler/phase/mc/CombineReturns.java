package compiler.phase.mc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCLabel;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public class CombineReturns extends AbstractMCCompilationPhase {

    public CombineReturns(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        // check if the function has multiple returns
        int returns = 0;
        for (AbstractMachineCode mc : cf.getMachineCode())
            if (mc.isRet()) {
                returns++;
                if (returns > 1)
                    break;
            }
        
        // if we have only one return MC, we dont need to do anything
        if (returns == 1)
            return;
        
        // combines MC
        
        List<AbstractMachineCode> newMCs = new LinkedList<AbstractMachineCode>();        
        MCLabel retLabel = new MCLabel("exit"); 
        
        for (AbstractMachineCode mc : cf.getMachineCode()) {
            AbstractMachineCode add = mc;
            
            if (mc.isRet()) {
                add = UVMCompiler.MCDriver.genJmp(retLabel);
            }
            
            newMCs.add(add);
        }
        
        AbstractMachineCode retMC = UVMCompiler.MCDriver.genRet();
        retMC.setLabel(retLabel);
        newMCs.add(retMC);
        
        cf.setMachineCode(newMCs);
    }

}
