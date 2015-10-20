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
    		writer.write("#include <stdlib.h>\n");
    		writer.write("\n");
    		
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
    		
    		// unwind table
    		writer.write("void initUnwindTable() {\n");
    		
    		// create unwind table
    		writer.write(String.format(
    				"unwindTable = (UnwindTable) malloc(%d * sizeof(UnwindTable)); \n\n", 
    				MicroVM.v.funcs.size()));
    		
    		// for every uvm func
    		for (Function f : MicroVM.v.funcs.values()) {
    			int id = f.getID();
    			
    			// funcID
    			writer.write(String.format(
    					"unwindTable[%d]->funcID = %d; \n", id, id));
    			
    			CompiledFunction cf = MicroVM.v.getCompiledFunc(f.getName());
    			StackManager stack = cf.stackManager;
    			
    			// callee saved registers
    			for (FrameSlot s : stack.getCalleeSavedRegisters()) {
    				writer.write(String.format(
    						"unwindTable[%d]->calleeSavedRegs.%s = %d; \n", id, s.getValue().REP().getName(), s.getOffset()));
    			}
    			writer.write('\n');
    			
    			// callsites
    			writer.write(String.format(
    					"unwindTable[%d]->callsitesN = %d; \n\n", id, stack.getCallerSavedRegisters().size()));
    			int i = 0;
    			for (AbstractCall callsite : stack.getCallerSavedRegisters().keySet()) {
    				// ret address
    				writer.write(String.format(
    						"unwindTable[%d]->callsites[%d]->returnAddress = (Address) %s; \n", id, i, StackManager.labelForCallsite(cf, callsite)));

    				// caller saved registers
    				for (FrameSlot slot : stack.getCallerSavedRegisters().get(callsite)) {
    					writer.write(String.format(
    							"unwindTable[%d]->callsites[%d]->callerSavedRegs.%s = %d; \n", 
    							id, i, slot.getValue().getName(), slot.getOffset()));
    				}
    				
    				// TODO: landing pad
    				
        			writer.write('\n');
        			
        			i++;
    			}
    			
    			writer.write('\n');
    		}
    		
    		writer.write("}\n");
    		
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
