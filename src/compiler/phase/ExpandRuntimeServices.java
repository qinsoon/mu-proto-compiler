package compiler.phase;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;

import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.MicroVM;
import uvm.Value;
import uvm.inst.InstCCall;
import uvm.inst.InstNew;
import uvm.runtime.RuntimeFunction;
import uvm.type.Int;

public class ExpandRuntimeServices extends AbstractCompilationPhase {

	public ExpandRuntimeServices(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitBasicBlock(BasicBlock bb) {
		List<Instruction> newInsts = new ArrayList<Instruction>();
		for (Instruction inst : bb.getInsts()) {
			
			if (inst.needsToCallRuntimeService()) {
				// we need to expand this instruction
				if (inst instanceof InstNew) {
					// for NEW, we emit a ccall to runtime allocator
					InstNew instNew = (InstNew) inst;
					
					verboseln("Instrumenting for NEW " + instNew.getType().prettyPrint());
					
					int alignment = instNew.getType().alignmentInBytes();
					int sizeRequired = instNew.getType().sizeInBytes() + MicroVM.v.objectModel.getHeaderSize(instNew.getType());
					
					verboseln("type size: " + instNew.getType().sizeInBytes() + ", header size: " + MicroVM.v.objectModel.getHeaderSize(instNew.getType()));
					verboseln("alignment: " + alignment);
					
					List<Value> args = new ArrayList<Value>();					
					args.add(new IntImmediate(Int.I64, (long)sizeRequired));
					args.add(new IntImmediate(Int.I64, (long)alignment));
					
					Instruction newInst = ccallRuntimeFunction(RuntimeFunction.allocObj, args);
					reserveLabel(inst, newInst);
					reserveDef(inst, newInst);
					newInsts.add(newInst);
					// TODO: and a ccall to init its header
				} else {
					UVMCompiler.error("unimplemented runtime service expansion for " + inst.getClass().getName());
				}
			} else {
				newInsts.add(inst);
			}
		}
		
		bb.setInstructions(newInsts);
	}
	
	private void reserveLabel(Instruction oldInst, Instruction newInst) {
		if (oldInst.getLabel() != null)
			newInst.setLabel(oldInst.getLabel());
	}
	
	private void reserveDef(Instruction oldInst, Instruction newInst) {
		if (oldInst.getDefReg() != null)
			newInst.setDefReg(oldInst.getDefReg());
	}
	
	private InstCCall ccallRuntimeFunction(RuntimeFunction rt, List<Value> args) {
		return new InstCCall(rt.getCallConv(), rt.getFunctionSignature(), rt.getFuncName(), args);
	}
}
