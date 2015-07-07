package compiler.phase.mc.x64;

import java.util.ArrayList;
import java.util.List;

import burm.mc.X64add;
import burm.mc.X64lea;
import burm.mc.X64load_mov;
import burm.mc.X64store_mov;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.Label;
import uvm.MicroVM;
import uvm.inst.AbstractCall;
import uvm.inst.InstNewStack;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCDispMemoryOperand;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCRegister;
import uvm.runtime.RuntimeFunction;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64MachineCodeExpansion extends AbstractMCCompilationPhase {

	public X64MachineCodeExpansion(String name, boolean verbose) {
		super(name, verbose);
	}	
	
	private MCRegister getTmpRegister(CompiledFunction cf, int tmpIndex, int dataType) {
		return cf.findOrCreateRegister("mcexp_tmp_" + tmpIndex, 
				MCRegister.OTHER_SYMBOL_REG, dataType);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		verboseln("---before mcexp on " + cf.getOriginFunction().getName() + "---");
		verboseln(cf.prettyPrint());
		
		int tmpIndex = 0;
		
//		for (int i = 0; i < cf.mc.size(); i++) {
		for (MCBasicBlock bb : cf.BBs) {
			for (int i = 0; i < bb.getMC().size(); i++) {
				AbstractMachineCode mc = bb.getMC().get(i);
				
				if (mc.isCall()) {
					AbstractCall hlCall = (AbstractCall) mc.getHighLevelIR();
					
					// _allocStack
					// we need to insert a sequence of machine code to init the stack
					if (hlCall.getFunc().equals(RuntimeFunction.allocStack.getFuncName())) {
						List<AbstractMachineCode> insertedCode = new ArrayList<AbstractMachineCode>();
						
						// get entryFunction name
						Label entryFuncLabel = (Label) hlCall.getArguments().get(1);
						String entryFuncName = entryFuncLabel.getName();
						Function entryFunc = MicroVM.v.getFunction(entryFuncName);
						
						// get stack location
						MCRegister stackStruct = mc.getDefineAsReg();
						// skip a int64 (see runtime.h)
						MCDispMemoryOperand stackStructSP = new MCDispMemoryOperand(stackStruct, 8);
						// load into a register
						MCRegister stackLoc = getTmpRegister(cf, tmpIndex++, MCRegister.DATA_GPR);
						X64load_mov load = new X64load_mov();
						load.setOperand0(stackStructSP);
						load.setDefine(stackLoc);
						insertedCode.add(load);
						
						MCDispMemoryOperand stackLocMem = new MCDispMemoryOperand(stackLoc, 0);
						
						// setup stack
						X64UVMCallConvention cc = new X64UVMCallConvention();
						insertedCode.addAll(cc.setupStackForThreadEntryFunction(cf, entryFunc, ((InstNewStack)hlCall.getOriginalInst()).getArguments(), stackLocMem));
						int stackDisp = cc.getStackDispForLastThreadEntryFunction();
						
						// save stack pointer to stack struct
						// 1. compute current stack pointer
						X64add add = new X64add();
						add.setOperand0(stackLoc);
						add.setOperand1(new MCIntImmediate(stackDisp));
						add.setDefine(stackLoc);
						insertedCode.add(add);
						// 2. store the stack pointer back to the struct
						X64store_mov store = new X64store_mov();
						store.setOperand0(stackStructSP);
						store.setOperand1(stackLoc);
						insertedCode.add(store);
						
						// insert into original mc
						bb.getMC().addAll(i+2, insertedCode);
						cf.addMachineCode(cf.getIndexOfMachineCode(mc) + 1, insertedCode);
					}
				}
			}
		}
		
		verboseln("---after mcexp on " + cf.getOriginFunction().getName() + "---");
		verboseln(cf.prettyPrint());
	}

}
