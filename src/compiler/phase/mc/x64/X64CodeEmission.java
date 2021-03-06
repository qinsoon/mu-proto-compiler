package compiler.phase.mc.x64;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import burm.mc.X64Driver;
import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.StackManager;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;
import compiler.phase.mc.AbstractMCCompilationPhase;
import uvm.*;
import uvm.inst.AbstractCall;
import uvm.mc.*;
import uvm.runtime.UVMRuntime;

public class X64CodeEmission extends AbstractMCCompilationPhase {
    String dir;
    
    public static final boolean EMIT_CFI = true;
    
    public X64CodeEmission(String name, String dir, boolean verbose) {
        super(name, verbose);
        this.dir = dir;
    }
    
    @Override
    protected void preChecklist() {
    	File fDir = new File(dir);
    	
    	if (!fDir.exists())
    		fDir.mkdirs();
    	else {
    		for (File f : fDir.listFiles()) {
    			f.delete();
    		}
    	}
    }
    
    @Override
    protected void postChecklist() {
    	File runtime = new File(UVMCompiler.BASE_DIR + "/" + UVMRuntime.LIB_PATH);
    	Path dst = Paths.get(dir + "/" + UVMRuntime.LIB_NAME);
    	
    	try {
			Files.copy(runtime.toPath(), dst, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("copy uvmrt from " + runtime.getAbsolutePath() + " to " + dst.toString());
		} catch (IOException e) {
			e.printStackTrace();
			UVMCompiler.error("error copying uvm runtime");
		}
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        BufferedWriter writer = null;
        String fileName = dir + "/" + cf.getOriginFunction().getName() + ".s"; 
        try {
            File outFile = new File(fileName);
            outFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(outFile));
            
            // constants
            writer.write(".align 8\n");
            for (MCConstant c : MCConstant.constants) {
                writer.write(UVMCompiler.MCDriver.emitOp(c.getLabel()) + ":");
                writer.write('\n');
                writer.write('\t');
                if (c.getValue().length == 1) {
                    // quad word
                    writer.write(".quad ");
                    writer.write(Long.toString(c.getValue()[0]));
                } else {
                    UVMCompiler.error("unimplemented constant emission for byte length: " + c.getValue().length);
                }
                writer.write('\n');
            }
            
            if (cf.getOriginFunction().getName().equals("main")) {
                writer.write("\t.globl _uvmMain\n");
                writer.write("\t.globl uvmMain\n");
                writer.write("_uvmMain:\n");
                writer.write("uvmMain:\n");
            } else {
                writer.write("\t.globl " + cf.getOriginFunction().getName() + "\n");
            }
            
            writer.write(cf.getOriginFunction().getName() + ":\n");
            if (EMIT_CFI) {
            	writer.write("\t.cfi_startproc\n");
            }
            
            if (!cf.prologue.isEmpty()) {
                for (AbstractMachineCode mc : cf.prologue)
                    emitMC(writer, cf, mc);
            }
            
            // we now start from a common main function, then create a thread to execute uvm main
            // so we dont need to init runtime in uvm main
//            if (cf.getOriginFunction().isMain() && MicroVM.v.runtime.needToInitRuntime()) {
//            	// emit call _initRuntime()
//            	emitMC(writer, UVMCompiler.MCDriver.genCall(new MCLabel(UVMRuntime.INIT_FUNC)));
//            }
            
            for (AbstractMachineCode mc : cf.finalMC) {
                emitMCInsertingEpilogueBeforeRet(writer, cf, mc);
            }            
            
            if (EMIT_CFI) {
            	writer.write("\t.cfi_endproc\n");
            }
        } catch (IOException e) {
            UVMCompiler.error("Error when emitting " + fileName);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    private void emitMCInsertingEpilogueBeforeRet(BufferedWriter writer, CompiledFunction cf, AbstractMachineCode mc) throws IOException {
        verboseln("Emiting: " + mc.prettyPrintOneline());
        if (mc.isRet()) {
            // write the label of 'ret' first
            if (mc.getLabel() != null) {
                writer.write(UVMCompiler.MCDriver.emitOp(mc.getLabel()) + ":");
                writer.write('\n');
            }
            
            // emit epilogue
            for (AbstractMachineCode epiMC : cf.epilogue)
                emitMC(writer, cf, epiMC);
            
            // emit ret
            writer.write('\t');
            writer.write(mc.emit());
            writer.write('\n');            
        } else emitMC(writer, cf, mc);
    }
    
    private static void emitMC(BufferedWriter writer, CompiledFunction cf, AbstractMachineCode mc) throws IOException {
        if (mc.getLabel() != null) {
        	MCBasicBlock bb = cf.getBasicBlock(mc.getLabel().getName());
        	if (bb != null && bb.isGlobalVisible()) {
        		writer.write(".global _" + StackManager.labelForExceptionBlock(cf, bb) + "\n");
        		writer.write("_" + StackManager.labelForExceptionBlock(cf, bb) + ":\n");
        	}
            writer.write(UVMCompiler.MCDriver.emitOp(mc.getLabel()) + ":");
            writer.write('\n');
        }
        writer.write('\t');
        writer.write(mc.emit());
        if (mc.getComment() != null) {
        	writer.write("\t\t#");
        	writer.write(mc.getComment());
        }
        
        writer.write('\n');
        
        if (mc.isCall() || mc.isCallWithExp()) {
        	if (mc.getHighLevelIR() instanceof AbstractCall) {
	        	// insert a label after the call
	        	String labelName = StackManager.labelForCallsite(cf, (AbstractCall) mc.getHighLevelIR());
                MCLabel label = new MCLabel(labelName);
	        	writer.write(".globl _" + UVMCompiler.MCDriver.emitOp(label) + "\n");
	        	writer.write("_" + UVMCompiler.MCDriver.emitOp(label) + ":");
	        	writer.write('\n');
        	}
        }
    }
}
