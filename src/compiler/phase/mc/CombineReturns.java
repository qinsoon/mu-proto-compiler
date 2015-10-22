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
    	
    	// since we are gonna remove existing ret, we save their label, and changing branching target to 'exit'
    	List<String> retLabels = new ArrayList<String>();	
    	
        int returns = 0;
        for (AbstractMachineCode mc : cf.getMachineCode())
            if (mc.isRet()) {
                returns++;
                if (mc.getLabel() != null)
                	retLabels.add(mc.getLabel().getName());
            }
        
        // if we have only one return MC, we dont need to do anything
        if (returns == 1)
            return;
        
        if (returns < 1)
        	// it is possible that a function does not return (it may only throws something)
//        	UVMCompiler.error("this func does not have a return");
        	return;
        
        // combines MC
        
        List<AbstractMachineCode> newMCs = new LinkedList<AbstractMachineCode>();        
        MCLabel retLabel = new MCLabel("exit"); 
        
        for (AbstractMachineCode mc : cf.getMachineCode()) {
            AbstractMachineCode add = mc;
            
            // replace ret with a jump to the common return inst
            if (mc.isRet()) {
                add = UVMCompiler.MCDriver.genJmp(retLabel);
            } 
            // if a inst jumps to the old return, we make it jump to the new common one
            else if (mc.isJump()) {
            	MCLabel l = (MCLabel) mc.getOperand(0);
            	if (retLabels.contains(l.getName())) {
            		mc.setOperand(0, retLabel);
            	}
            }
            
            newMCs.add(add);
        }
        
        AbstractMachineCode retMC = UVMCompiler.MCDriver.genRet();
        retMC.setLabel(retLabel);
        newMCs.add(retMC);
        
        cf.setMachineCode(newMCs);
    }

}
