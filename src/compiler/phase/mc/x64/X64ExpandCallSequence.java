package compiler.phase.mc.x64;

import java.util.ArrayList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.MicroVM;
import uvm.inst.AbstractCall;
import uvm.inst.InstCall;
import uvm.inst.InstCCall;
import uvm.inst.InstCallWithException;
import uvm.inst.InstTailCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64ExpandCallSequence extends AbstractMCCompilationPhase {

    public X64ExpandCallSequence(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        X64UVMCallConvention cc = new X64UVMCallConvention();
        
        cc.genPrologue(cf);
        cc.genEpilogue(cf);
        
        for (MCBasicBlock bb : cf.BBs) {
            List<AbstractMachineCode> newMC = new ArrayList<AbstractMachineCode>();
            
            for (AbstractMachineCode mc : bb.getMC()) {
            	IRTreeNode HLLIR = mc.getHighLevelIR();
                if (HLLIR instanceof AbstractCall) {
                	
                	MCLabel mcLabel = mc.getLabel();
                	if (mcLabel != null)
                		mc.setLabel(null);
                	
                	// uVM internal calling conv
                	if (HLLIR instanceof InstCall || HLLIR instanceof InstCallWithException || HLLIR instanceof InstTailCall) {
	                    // use this instance for this call only
	                    X64UVMCallConvention cc1 = new X64UVMCallConvention();
	                    
	                    // expand calling sequence
	                    MCLabel label = (MCLabel) mc.getOperand(0);
	                    Function callee = MicroVM.v.getFunction(label.getName());
	                    newMC.addAll(cc1.callerSetupCallSequence(cf, (AbstractCall) HLLIR, mc));
	                    newMC.addAll(cc1.callerCleanupCallSequence(cf, (AbstractCall) HLLIR, mc));
                	} 
                	// C calling conv
                	else if (HLLIR instanceof InstCCall) {
                		if (((InstCCall) HLLIR).getCallConv() == InstCCall.CC_DEFAULT) {
                			X64CDefaultCallConvention cc1 = new X64CDefaultCallConvention();
                			
                			newMC.addAll(cc1.callerSetupCallSequence(cf, (AbstractCall) HLLIR, mc));
                			newMC.addAll(cc1.callerCleanupCallSequence(cf, (AbstractCall) HLLIR, mc));
                		} else {
                			UVMCompiler.error("unimplemented c call conv:" + ((InstCCall) HLLIR).getCallConv());
                		}
                	}
                	else {
                		UVMCompiler.error("unknown calling instruction:" + HLLIR.toString());
                	}
                	
                	if (mcLabel != null) {
                		newMC.get(0).setLabel(mcLabel);
                	}
                } else {
                    newMC.add(mc);
                }
            }
            
            bb.setMC(newMC);
        }
    }

}
