package compiler.phase;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;
import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.MicroVM;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstCCall;
import uvm.inst.InstNew;
import uvm.runtime.RuntimeFunction;
import uvm.type.Int;

public class ExpandRuntimeServices extends AbstractCompilationPhase {
	static int tempIndex = 0;
	
	private static int getNewTempIndex() {
		return tempIndex++;
	}

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
					Type t = instNew.getType();
					verboseln("Instrumenting for NEW " + t.prettyPrint());
					
					int alignment = instNew.getType().alignmentInBytes();
					int sizeRequired = instNew.getType().sizeInBytes() + MicroVM.v.objectModel.getHeaderSize(t);
					
					verboseln("type size: " + instNew.getType().sizeInBytes() + ", header size: " + MicroVM.v.objectModel.getHeaderSize(t));
					verboseln("alignment: " + alignment);
					
					List<Value> args = new ArrayList<Value>();					
					args.add(new IntImmediate(Int.I64, (long)sizeRequired));
					args.add(new IntImmediate(Int.I64, (long)alignment));
					
					Instruction allocObj = ccallRuntimeFunction(RuntimeFunction.allocObj, args);
					reserveLabel(inst, allocObj);
					reserveDef(inst, allocObj);
					newInsts.add(allocObj);
					
					// ccall to init its header
					List<Value> args2 = new ArrayList<Value>();
					args2.add(allocObj.getDefReg());
					args2.add(new IntImmediate(Int.I64, MicroVM.v.objectModel.getHeaderInitialization(t)));
					
					Instruction initObj = ccallRuntimeFunction(RuntimeFunction.initObj, args2);
					newInsts.add(initObj);
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
