package compiler.phase.mc;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCLabeledMemoryOperand;
import uvm.mc.MCRegister;
import uvm.runtime.UVMRuntime;

public class InsertYieldpoint extends AbstractMCCompilationPhase {

	public InsertYieldpoint(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		verboseln("----- insert yieldpoint for " + cf.getOriginFunction().getName() + " -----");
		
		// at prologue
		AbstractMachineCode yieldpoint = genYieldpoint(cf); 
		cf.prologue.add(yieldpoint);
		verboseln("for prologue: " + yieldpoint.prettyPrint());
		
		// at every backedge
		for (MCBasicBlock bb : cf.BBs) {
			if (!bb.getBackEdges().isEmpty()) {
				verboseln("for backedge at " + bb.getName() + ": " + yieldpoint.prettyPrint());
				int whereToInsert = bb.getMC().size() - 1;
				bb.getMC().add(whereToInsert, yieldpoint);
			}
		}
	}
	
	private static AbstractMachineCode genYieldpoint(CompiledFunction cf) {
		MCLabeledMemoryOperand dst = new MCLabeledMemoryOperand();
		dst.setDispLabel(new MCLabel(UVMRuntime.YIELDPOINT_PROTECT_AREA));
		dst.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
		dst.setSize((byte) 8);
		
		MCIntImmediate src = new MCIntImmediate(UVMRuntime.YIELDPOINT_WRITE);
		AbstractMachineCode yieldpoint = UVMCompiler.MCDriver.genMove(dst, src);
		yieldpoint.setComment("yieldpoint");
		
		return yieldpoint;
	}
}
