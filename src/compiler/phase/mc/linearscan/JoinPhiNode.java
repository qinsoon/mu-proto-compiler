package compiler.phase.mc.linearscan;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class JoinPhiNode extends AbstractMCCompilationPhase {

	public JoinPhiNode(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		for (AbstractMachineCode mc : cf.getMachineCode()) {
			if (mc.isPhi()) {
				MCRegister def = mc.getDefineAsReg();
				for (int i = 1; i < mc.getNumberOfOperands(); i += 2) {
					MCOperand op = mc.getOperand(i - 1);
					if (op instanceof MCRegister && op != def) {
						((MCRegister) op).setREP(def);
					}
				}
			}
		}
	}

}
