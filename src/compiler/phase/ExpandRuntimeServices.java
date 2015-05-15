package compiler.phase;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;
import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.MicroVM;
import uvm.Register;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstCCall;
import uvm.inst.InstGetFieldIRef;
import uvm.inst.InstGetIRef;
import uvm.inst.InstNew;
import uvm.inst.InstNewStack;
import uvm.inst.InstNewThread;
import uvm.inst.InstStore;
import uvm.runtime.RuntimeFunction;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.type.Struct;

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
			
			if (inst.needsToExpandIntoRuntimeCall()) {
				// we need to expand this instruction
				
				// NEW:
				// a = call allocObj(...)
				// call initObj(a, ...)
				if (inst instanceof InstNew) {
					// for NEW, we emit a ccall to runtime allocator
					InstNew instNew = (InstNew) inst;
					Type t = instNew.getType();
					verboseln("Expanding NEW " + t.prettyPrint());
					
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
				}
				
				// NEWSTACK:
				// %argStruct = call allocObj(@argTmpType)
				// initObj(%argStruct, ...)
				// %a1 = GETIREF <@argTmpType 0> %args
				// STORE %a1 %arg1 
				// %a2 = GETIREF <@argTmpType 1> %args
				// STORE %a2 %arg2
				// ...
				// %s = call allocStack(65535, entryFunc, %argStruct)
				else if (inst instanceof InstNewStack) {
					InstNewStack instNewStack = (InstNewStack) inst;					
					verboseln("Expanding NEWSTACK " + instNewStack.getEntryFunction().getName());
					
					List<Value> args3;
					Instruction allocStack;
					
					// get a temp arg type
					if (!instNewStack.getEntryFunction().getSig().getParamTypes().isEmpty()) {
						Struct argStructType = Struct.findOrCreateStruct(instNewStack.getEntryFunction().getSig().getParamTypes());
						
						// %argStruct = call allocObj(@argTmpType)
						int alignment = argStructType.alignmentInBytes();
						int sizeRequired = argStructType.sizeInBytes() + MicroVM.v.objectModel.getHeaderSize(argStructType);
						
						verboseln("create a struct for args: " + argStructType.prettyPrint());
						
						List<Value> args1 = new ArrayList<Value>();
						args1.add(new IntImmediate(Int.I64, (long)sizeRequired));
						args1.add(new IntImmediate(Int.I64, (long)alignment));
						
						Instruction allocObj = ccallRuntimeFunction(RuntimeFunction.allocObj, args1);
						reserveLabel(inst, allocObj);
						Register entryArgs = bb.getFunction().findOrCreateRegister("exprt"+getNewTempIndex(), argStructType);
						allocObj.setDefReg(entryArgs);
						newInsts.add(allocObj);
						
						// initObj(%argStruct, ...)
						List<Value> args2 = new ArrayList<Value>();
						args2.add(entryArgs);
						args2.add(new IntImmediate(Int.I64, MicroVM.v.objectModel.getHeaderInitialization(argStructType)));
						
						Instruction initObj = ccallRuntimeFunction(RuntimeFunction.initObj, args2);
						newInsts.add(initObj);
						
						// %a1 = GETIREF <@argTmpType 0> %args
						// STORE %a1 %arg1 
						for (int i = 0; i < instNewStack.getArguments().size(); i++) {
							Instruction getiref = new InstGetFieldIRef(argStructType, i, entryArgs);
							IRef irefType = IRef.findOrCreateIRef(argStructType.getType(i));
							Register tmpIRef = bb.getFunction().findOrCreateRegister("exprt_arg_tmp_iref" + getNewTempIndex(), irefType);
							newInsts.add(getiref);
							
							Instruction store = new InstStore(irefType, tmpIRef, instNewStack.getArguments().get(i));
							newInsts.add(store);
						}
						
						// %s = call allocStack(65535, entryFunc, %argStruct)
						args3 = new ArrayList<Value>();
						args3.add(new IntImmediate(Int.I64, (long)Stack.T.sizeInBytes()));
						args3.add(instNewStack.getEntryFunction().getFuncLabel());
						args3.add(entryArgs);
						
						allocStack = ccallRuntimeFunction(RuntimeFunction.allocStack, args3);
						reserveDef(inst, allocStack);
					} else {
						// %s = call allocStack(65535, entryFunc, 0)
						args3 = new ArrayList<Value>();
						args3.add(new IntImmediate(Int.I64, (long)Stack.T.sizeInBytes()));
						args3.add(instNewStack.getEntryFunction().getFuncLabel());
						args3.add(new IntImmediate(Int.I64, (long)0));
						
						allocStack = ccallRuntimeFunction(RuntimeFunction.allocStack, args3);
						reserveDef(inst, allocStack);
						reserveLabel(inst, allocStack);
					}					

					newInsts.add(allocStack);
				}
				
				// NEWTHREAD:
				// 
				else if (inst instanceof InstNewThread) {
					InstNewThread instNewThread = (InstNewThread) inst;
					
					uvm.Register stack = instNewThread.getStack();
					
					List<Value> args = new ArrayList<Value>();
					args.add(stack);
					
					Instruction newThread = ccallRuntimeFunction(RuntimeFunction.newThread, args);
					reserveDef(inst, newThread);
					reserveLabel(inst, newThread);
					newInsts.add(newThread);
				}
				else {
					UVMCompiler.error("unimplemented runtime service expansion for " + inst.getClass().getName());
				}
			} else {
				newInsts.add(inst);
			}
		}
		
		bb.setInstructions(newInsts);
		
		verboseln("After expanding runtime services for BB " + bb.getName());
		for (Instruction i : bb.getInsts())
			verboseln(i.prettyPrint());
		verboseln();
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
