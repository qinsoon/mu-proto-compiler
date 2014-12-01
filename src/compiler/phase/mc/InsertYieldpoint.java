package compiler.phase.mc;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCCGlobal;
import uvm.mc.MCIntImmediate;
import uvm.runtime.UVMRuntime;

public class InsertYieldpoint extends AbstractMCCompilationPhase {

	public InsertYieldpoint(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		verboseln("----- insert yieldpoint for " + cf.getOriginFunction().getName() + " -----");
		
		// at prologue
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
	
	private static final AbstractMachineCode yieldpoint;
	
	static {
		MCCGlobal dst = new MCCGlobal(UVMRuntime.YIELDPOINT_PROTECT_AREA);
		MCIntImmediate src = new MCIntImmediate(UVMRuntime.YIELDPOINT_WRITE);
		yieldpoint = UVMCompiler.MCDriver.genMove(dst, src);
		yieldpoint.setComment("yieldpoint");
	}
}
