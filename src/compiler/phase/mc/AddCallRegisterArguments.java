package compiler.phase.mc;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.IRTreeNode;
import uvm.Register;
import uvm.inst.AbstractCall;
import uvm.inst.InstCCall;
import uvm.inst.InstCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCRegister;

public class AddCallRegisterArguments extends AbstractMCCompilationPhase {

	public AddCallRegisterArguments(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		for (MCBasicBlock bb : cf.BBs) {
			for (AbstractMachineCode mc : bb.getMC()) {
				if (mc.isCall() || mc.isCallWithExp() || mc.isTailCall()) {
					IRTreeNode hir = mc.getHighLevelIR();
					
					verboseln("Adding register arguments for " + hir.prettyPrint());
					
					if (hir instanceof AbstractCall) {
						AbstractCall c = (AbstractCall) hir;
						
						for (uvm.Value v : c.getArguments()) {
							if (v instanceof Register) {
								MCRegister reg = cf.findOrCreateRegister(((Register) v).getName(), MCRegister.OTHER_SYMBOL_REG, MCRegister.dataTypeFromOpCode(hir.getOpcode()));
								mc.addImplicitUse(reg);
								verboseln("  " + reg.prettyPrint() + " as implicit use");
							}
						}						
					} else {
						UVMCompiler.error("unexpected HIR to be translated into call: " + hir.getClass());
					}
				}
			}
		}
	}

}
