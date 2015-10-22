package compiler.phase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import uvm.CompiledFunction;
import uvm.FrameSlot;
import uvm.Function;
import uvm.MicroVM;
import uvm.StackManager;
import uvm.Type;
import uvm.inst.AbstractCall;
import uvm.inst.InstCallWithException;
import uvm.mc.MCBasicBlock;
import uvm.runtime.UVMRuntime;
import uvm.type.Array;
import compiler.UVMCompiler;

/**
 * this generates code that will be used by GC and runtime
 * be sure this part matches with runtime code 
 * 
 */
public class RuntimeCodeEmission extends AbstractCompilationPhase {
	String dir;

	public RuntimeCodeEmission(String name, String dir, boolean verbose) {
		super(name, verbose);
		this.dir = dir;
	}

	@Override
    protected void preChecklist() {
    	BufferedWriter writer = null;
    	String fileName = dir + "/typeInfo.c";
    	
    	try {
    		File out = new File(fileName);
    		writer = new BufferedWriter(new FileWriter(out));
    		
    		// include runtime.h
    		writer.write("#include \"typeinfo.h\"\n");
    		writer.write("#include \"threadstack.h\"\n");
    		writer.write("#include <stdlib.h>\n");
    		writer.write("\n");
    		
    		initTypeTable(writer);
    		
    		initUnwindTable(writer);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		UVMCompiler.error("Error when emitting runtime code");
    	} finally {
    		if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    	}
    }

	private void initUnwindTable(BufferedWriter writer) throws IOException {
		StringBuilder funcBody = new StringBuilder();
		List<String> usedLabels = new ArrayList<String>();
		
		// unwind table
		funcBody.append("void initUnwindTable() {\n");
		
		// create unwind table
		funcBody.append(String.format(
				"unwindTable = (UnwindTable**) malloc(%d * sizeof(UnwindTable*)); \n", 
				MicroVM.v.funcs.size()));
		funcBody.append(String.format(
				"unwindTableCount = %d; \n\n", MicroVM.v.funcs.size()));
		
		// for every uvm func
		for (Function f : MicroVM.v.funcs.values()) {
			funcBody.append("//" + f.getName() + "\n");
			
			int id = f.getID();
			CompiledFunction cf = MicroVM.v.getCompiledFunc(f.getName());
			StackManager stack = cf.stackManager;
			
			String var = "unwind" + id;
			
			funcBody.append(String.format(
					"UnwindTable* %s = allocateUnwindTable(%d);\n",
					var, stack.getCallerSavedRegisters().size()));
			funcBody.append(String.format(
					"unwindTable[%d] = %s; \n",
					id, var));
			
			// funcID
			funcBody.append(String.format(
					"%s->funcID = %d; \n", var, id)); 			

			
			// callee saved registers
			for (FrameSlot s : stack.getCalleeSavedRegisters()) {
				funcBody.append(String.format(
						"%s->calleeSavedRegs.%s = %d; \n", var, s.getValue().REP().getName(), s.getOffset()));
			}
			
			// callsites
			funcBody.append(String.format(
					"%s->callsitesN = %d; \n", var, stack.getCallerSavedRegisters().size()));
			int i = 0;
			for (AbstractCall callsite : stack.getCallerSavedRegisters().keySet()) {
				funcBody.append("//" + callsite.prettyPrint() + "\n");
				// ret address
				String label = StackManager.labelForCallsite(cf, callsite);
				funcBody.append(String.format(
						"%s->callsites[%d].returnAddress = (Address) %s; \n", var, i, label));
				usedLabels.add(label);

				// caller saved registers
				for (FrameSlot slot : stack.getCallerSavedRegisters().get(callsite)) {
					funcBody.append(String.format(
							"%s->callsites[%d].callerSavedRegs.%s = %d; \n", 
							var, i, slot.getValue().getName(), slot.getOffset()));
				}
				
				// landing pad
				if (callsite instanceof InstCallWithException) {
					String exceptionLabelName = ((InstCallWithException) callsite).getExceptionLabel().getName();
					MCBasicBlock exceptionBB = cf.getBasicBlock(exceptionLabelName);
					String exceptionLabel = StackManager.labelForExceptionBlock(cf, exceptionBB);
					
					funcBody.append(String.format(
							"%s->callsites[%d].landingPad = (Address) %s; \n", var, i, exceptionLabel));
					usedLabels.add(exceptionLabel);
				}
				
				funcBody.append('\n');
				
				i++;
			}
		}
		
		funcBody.append("}\n");
		
		// actual write
		for (String label : usedLabels)
			writer.write("extern Address " + label + ";\n");
		writer.write('\n');
		
		writer.write(funcBody.toString());
	}

	private void initTypeTable(BufferedWriter writer) throws IOException {
		// declare typeInfo table
		writer.write(String.format("extern TypeInfo** typeInfoTable; \n"));
		writer.write(String.format("extern int typeCount;\n"));
		writer.write("\n");
		
		// init typeTable
		writer.write("void initTypeTable() {\n");
		
		writer.write(String.format("typeCount = %d; \n", MicroVM.v.getTypesByIDMap().size()));
		writer.write(String.format("typeInfoTable = (TypeInfo**) malloc(sizeof(TypeInfo*) * %d); \n", MicroVM.v.getTypesByIDMap().size()));
		
		for (int id = 0; id < MicroVM.v.getTypesByIDMap().size(); id++) {
			Type t = MicroVM.v.getTypeByID(id);
			int size = t.sizeInBytes();
			int align = t.alignmentInBytes();
			int[] refOffsets  = MicroVM.v.objectModel.getBaseRefOffsets(t);
			int[] irefOffsets = MicroVM.v.objectModel.getIRefOffsets(t); 
			
			String curTypeInfo = String.format("tid%d", id);
			
			writer.write(String.format("// ID=%d: %s\n", id, t.prettyPrint()));
			
			writer.write(String.format("TypeInfo* %s = ", curTypeInfo));
			if (t instanceof Array) {
				Array arrayT = (Array) t;
				int eleSize = arrayT.getEleType().sizeInBytes();
				int length = arrayT.getLength();
				
				// allocArrayTypeInfo(id, eleSize, length, align, nRefOffsets, nIRefOffsets)
				writer.write(String.format(
						"allocArrayTypeInfo(%d, %d, %d, %d, %d, %d);\n", 
						id, eleSize, length, align, refOffsets.length, irefOffsets.length));
			} else {
				// scalar or hybrid(unimplemented)
				// scalar then
				
				// allocScalarTypeInfo(id, size, align, nRefOffsets, nIRefOffsets)
				writer.write(String.format(
						"allocScalarTypeInfo(%d, %d, %d, %d, %d);\n", 
						id, size, align, refOffsets.length, irefOffsets.length));
			}    			
			
			// refs
			// tid0->refOffsets[0] = x;
			for (int i = 0; i < refOffsets.length; i++) {
				writer.write(String.format(
						"%s->refOffsets[%d] = %s;\n", 
						curTypeInfo, i, refOffsets[i]));
			}
			
			// irefs
			// tid0->refOffsets[nRef + i] = x;
			for (int i = 0; i < irefOffsets.length; i++) {
				writer.write(String.format(
						"%s->refOffsets[%d] = %s;\n", 
						curTypeInfo, refOffsets.length + i, irefOffsets[i]));
			}
			
			// save the type info in the table
			// types[0] = tid0;
			writer.write(String.format("typeInfoTable[%d] = %s;\n", id, curTypeInfo));
			writer.write("\n");
		}
		
		writer.write("}\n");
	}
	
	@Override
	protected void postChecklist() {
		// copy headers
		List<File> headers = new ArrayList<File>();
		headers.add(new File(UVMCompiler.BASE_DIR + "/" + UVMRuntime.RUNTIME_SOURCE_PATH + "runtime.h"));
		headers.add(new File(UVMCompiler.BASE_DIR + "/" + UVMRuntime.RUNTIME_SOURCE_PATH + "osx_ucontext.h"));
		headers.add(new File(UVMCompiler.BASE_DIR + "/" + UVMRuntime.RUNTIME_SOURCE_PATH + "linux_ucontext.h"));

		for (File f : headers) {
			Path dst = Paths.get(dir + "/" + f.getName());
			
			try {
				Files.copy(f.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("copy runtime.h from " + f.getAbsolutePath() + " to " + dst.toString());
			} catch (IOException e) {
				e.printStackTrace();
				UVMCompiler.error("error copying runtime headers");
			}
		}
		
		// copy MakeFile
		File make = new File(UVMCompiler.BASE_DIR + "/EmitMakefile");
		Path makeDst = Paths.get(dir + "/Makefile");
		try {
			Files.copy(make.toPath(), makeDst, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("copy makefile EmitMakefile to " + makeDst.toString());
		} catch (IOException e) {
			e.printStackTrace();
			UVMCompiler.error("error copying Makefile after compilation");
		}
	}
}
