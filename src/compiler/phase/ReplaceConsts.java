package compiler.phase;

import uvm.ImmediateValue;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Value;

public class ReplaceConsts extends AbstractCompilationPhase {

	public ReplaceConsts(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitInstruction(Instruction inst) {
		verboseln("checking " + inst.prettyPrint());
		for (int i = 0; i < inst.getOperands().size(); i++) {
			Value op = inst.getOperands().get(i);
			verbose("  op:" + op.prettyPrint());
			
			if (op instanceof uvm.Register) {
				String id = ((uvm.Register) op).getName();
				ImmediateValue immV = MicroVM.v.getGlobalConsts(id);
				
				if (immV != null) {
					verboseln("->CONST");
					inst.getOperands().set(i, immV);
				} else{
					verboseln();
				}
			} else {
				verboseln();
			}
		}
	}
}
