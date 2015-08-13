package uvm.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.ImmediateValue;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Type;
import uvm.mc.AbstractMachineCode;

public class RecordUVMStats {
	String path;
	
	public RecordUVMStats(String path) {
		this.path = path;
	}
	
	private PrintWriter getPrintWriter(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
		return new PrintWriter(path + fileName, "UTF-8");
	}
	
	public final void output() {
		uvmInfo();
		for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
			compiledFuncInfo(cf);
		}
	}
	
	private void uvmInfo() {
		try{
			PrintWriter writer = getPrintWriter("uvm.txt");
			
			// types
			writer.println("TYPES:");
			for (String s : MicroVM.v.types.keySet()) {
				Type t = MicroVM.v.types.get(s);
				writer.println(s + "=" + t.prettyPrint());
			}
			writer.println();
			
			// functions
			writer.println("FUNCTIONS:");
			for (String s : MicroVM.v.funcs.keySet()) {
				Function f = MicroVM.v.funcs.get(s);
				writer.println(s + "=" + f.getSig().prettyPrint());
			}
			writer.println();
			
			// global labels
			writer.println("GLOBAL LABELS:");
			for (String s : MicroVM.v.globalLabels.keySet()) {
				writer.println(s);
			}
			writer.println();
			
			// global consts
			writer.println("GLOBAL CONSTANTS:");
			for (String s : MicroVM.v.globalConsts.keySet()) {
				ImmediateValue v = MicroVM.v.globalConsts.get(s);
				writer.println(s + "=" + v.prettyPrint());
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void compiledFuncInfo(CompiledFunction cf) {
		try {
			PrintWriter writer = getPrintWriter(cf.getOriginFunction().getName() + ".txt");
			
			Function f = cf.getOriginFunction();
			writer.println("IR:");
			for (BasicBlock bb : f.getBBs()) {
				for (Instruction inst : bb.getInsts()) {
					writer.print(inst.prettyPrintWithDef());
					writer.println("  NODE" + inst.getId());
				}
				
			}
			writer.println();
			
			writer.println("Tree IR:");
			writer.println(f.printIRTree());
			writer.println();
			
			writer.println("Instruction Selection:");
			writer.println(f.printInstructionSelectionMatching());
			writer.println();
			
			writer.println("traverse code by BBs:");
			writer.println(cf.prettyPrint());
			writer.println();
			
			writer.println("serialized code (cf.mc), size=" + cf.getMachineCode().size() + ":");
			for (AbstractMachineCode mc : cf.getMachineCode()) {
				writer.println("#" + mc.sequence + " " + mc.prettyPrint());
			}
			writer.println();
			
			writer.println("final MC:");
			if (cf.finalMC == null)
				writer.println("not ready yet");
			else {
				for (AbstractMachineCode mc : cf.finalMC) {
					writer.println(mc.prettyPrint());
				}
			}
			writer.println();
			
			writer.println("intervals:");
			writer.println(cf.printIntervalString());
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
