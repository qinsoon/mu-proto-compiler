package compiler.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import compiler.UVMCompiler;
import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.MicroVM;
import uvm.Register;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstCCall;
import uvm.inst.InstGetElemIRefConstIndex;
import uvm.inst.InstGetFieldIRef;
import uvm.inst.InstGetIRef;
import uvm.inst.InstInternalIRefOffset;
import uvm.inst.InstInternalPrintStr;
import uvm.inst.InstLoadInt;
import uvm.inst.InstNew;
import uvm.inst.InstNewStack;
import uvm.inst.InstNewThread;
import uvm.inst.InstStore;
import uvm.inst.InstThreadExit;
import uvm.runtime.RuntimeFunction;
import uvm.type.Array;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Stack;
import uvm.type.Struct;

public class ExpandRuntimeServices extends AbstractCompilationPhase {
	static int tempIndex = 0;
	
	private static Register getNewTempRegister(Function f, Type t) {
		Register ret = f.findOrCreateRegister("tmp" + tempIndex, t);
		tempIndex++;
		return ret;
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
					allocObj.setOriginalInst(inst);
					newInsts.add(allocObj);
					
					// ccall to init its header
					List<Value> args2 = new ArrayList<Value>();
					args2.add(allocObj.getDefReg());
					args2.add(new IntImmediate(Int.I64, MicroVM.v.objectModel.getHeaderInitialization(t)));
					
					Instruction initObj = ccallRuntimeFunction(RuntimeFunction.initObj, args2);
					newInsts.add(initObj);
				}
				
				// NEWSTACK:
				// %s = call allocStack(65535, entryFunc, %argStruct)
				else if (inst instanceof InstNewStack) {
					InstNewStack instNewStack = (InstNewStack) inst;					
					verboseln("Expanding NEWSTACK " + instNewStack.getEntryFunction().getName());
					
					List<Value> args3;
					
					// %s = call allocStack(65535, entryFunc, 0)
					args3 = new ArrayList<Value>();
					args3.add(new IntImmediate(Int.I64, (long)Stack.T.sizeInBytes()));
					args3.add(instNewStack.getEntryFunction().getFuncLabel());
					args3.add(new IntImmediate(Int.I64, (long)0));
					
					Instruction allocStack = ccallRuntimeFunction(RuntimeFunction.allocStack, args3);
					reserveDef(inst, allocStack);
					reserveLabel(inst, allocStack);
					allocStack.setOriginalInst(inst);

					newInsts.add(allocStack);
					
					// if we need to init stack
					if (!instNewStack.getEntryFunction().getSig().getParamTypes().isEmpty()) {
						// refer to runtime.h
						// skip the first int64_t field, and we have the address of sp
						Register stack = allocStack.getDefReg();
						Instruction irefAdd = new InstInternalIRefOffset(stack, new IntImmediate(Int.I64, (long)64));
						Register spAddress = getNewTempRegister(bb.getFunction(), IRef.IREF_VOID);
						irefAdd.setDefReg(spAddress);
						
						newInsts.add(irefAdd);
						
						// load that address, then we have sp
						Instruction loadSP = new InstLoadInt(IRef.IREF_VOID, spAddress);
						Register sp = getNewTempRegister(bb.getFunction(), IRef.IREF_VOID);
						loadSP.setDefReg(sp);
						
						newInsts.add(loadSP);
					}
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
				
				// THREADEXIT:
				// ccall threadExit()
				else if (inst instanceof InstThreadExit) {					
					Instruction threadExit = ccallRuntimeFunction(RuntimeFunction.threadExit, new ArrayList<uvm.Value>());
					reserveLabel(inst, threadExit);
					newInsts.add(threadExit);
				}
				
				else if (inst instanceof InstInternalPrintStr) {
					InstInternalPrintStr printStr = (InstInternalPrintStr) inst;
					long[] int64array = printStr.stringLiteralToInt64();
					
					// malloc
					List<Value> args = new ArrayList<Value>();
					args.add(new IntImmediate(Int.I64, int64array.length * Int.I64.sizeInBytes()));
					Instruction malloc = ccallRuntimeFunction(RuntimeFunction.malloc, args);
					Register s = getNewTempRegister(bb.getFunction(), IRef.IREF_VOID);
					malloc.setDefReg(s);
					reserveLabel(inst, malloc);
					newInsts.add(malloc);
					
					for (int i = 0; i < int64array.length; i++) {
						// %si = GETELEMIREF %si i
						Instruction getElem = new InstGetElemIRefConstIndex(Array.findOrCreate(Int.I64, int64array.length), i, s);
						Register si = getNewTempRegister(bb.getFunction(), IRef.findOrCreateIRef(Int.I64));
						getElem.setDefReg(si);
						newInsts.add(getElem);
						
						// STORE %si int64array[i]
						Instruction store = new InstStore(IRef.findOrCreateIRef(Int.I64), si, new IntImmediate(Int.I64, int64array[i]));
						newInsts.add(store);
					}
					
					Instruction callPrintStr = ccallRuntimeFunction(RuntimeFunction.uvmPrintStr, Arrays.asList(s));
					newInsts.add(callPrintStr);
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
