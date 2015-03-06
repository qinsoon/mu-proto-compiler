package compiler.phase.mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCLabeledMemoryOperand;
import uvm.mc.MCRegister;
import uvm.runtime.RuntimeFunction;
import uvm.runtime.UVMRuntime;

public class InsertYieldpoint extends AbstractMCCompilationPhase {
	private static final boolean INSERT_PROLOGUE_YP = true;
	private static final boolean INSERT_EPILOGUE_YP = false;
	private static final boolean INSERT_BACKEDGE_YP = true;

	public InsertYieldpoint(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		verboseln("----- insert yieldpoint for " + cf.getOriginFunction().getName() + " -----");
		
		yieldpoint_id = 0;
		
		// at prologue
		if (INSERT_PROLOGUE_YP) {
			cf.prologue.addAll(genCheckingYieldpoint(cf));
			verboseln("for prologue");
		}
		
		// at every backedge
		if (INSERT_BACKEDGE_YP)
			for (MCBasicBlock bb : cf.BBs) {
				if (!bb.getBackEdges().isEmpty()) {
					verboseln("for backedge at " + bb.getName());
					int whereToInsert = bb.getMC().size() - 1;
					bb.getMC().addAll(whereToInsert, genCheckingYieldpoint(cf));
				}
			}
		
		// at epilogue
		if (INSERT_EPILOGUE_YP) {
			cf.epilogue.addAll(genCheckingYieldpoint(cf));
			verboseln("for epilogue");
		}
	}
	
	private int yieldpoint_id;
	
	private List<AbstractMachineCode> genCheckingYieldpoint(CompiledFunction cf) {
		MCLabeledMemoryOperand check = new MCLabeledMemoryOperand();
		check.setDispLabel(new MCLabel(UVMRuntime.YIELDPOINT_CHECK));
		check.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
		check.setSize((byte) 8);
		
		MCIntImmediate enabled = new MCIntImmediate(UVMRuntime.YIELDPOINT_ENABLE);
		
		List<AbstractMachineCode> ret = Arrays.asList(UVMCompiler.MCDriver.genCallIfEqual(enabled, check, new MCLabel(RuntimeFunction.yieldpoint.getFuncName()), yieldpoint_id));
		yieldpoint_id++;
		
		ret.get(0).setComment("checking yieldpoint");
		return ret;
	}
	
	@Deprecated
	private List<AbstractMachineCode> genPageProtectionYieldpoint(CompiledFunction cf) {
		MCLabeledMemoryOperand dst = new MCLabeledMemoryOperand();
		dst.setDispLabel(new MCLabel(UVMRuntime.YIELDPOINT_PROTECT_AREA));
		dst.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
		dst.setSize((byte) 8);
		
		MCIntImmediate src = new MCIntImmediate(UVMRuntime.YIELDPOINT_WRITE);
		AbstractMachineCode yieldpoint = UVMCompiler.MCDriver.genMove(dst, src);
		yieldpoint.setComment("page protection yieldpoint");
		
		List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
		ret.add(yieldpoint);
		
		return ret;
	}
}
