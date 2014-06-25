package compiler.phase.mc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import burm.mc.X64Driver;
import uvm.CompiledFunction;
import uvm.MicroVM;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;
import uvm.*;
import uvm.mc.*;

public class CodeEmission extends AbstractMCCompilationPhase {
    String dir;
    
    public CodeEmission(String name, String dir) {
        super(name);
        this.dir = dir;
    }    

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        BufferedWriter writer = null;
        String fileName = dir + "/" + cf.getOriginFunction().getName() + ".s"; 
        try {
            File outFile = new File(fileName);
            outFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(outFile));
            
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
                writer.write("\t.globl _main\n");
                writer.write("_main:\n");
            } else {
                writer.write("\t.globl " + cf.getOriginFunction().getName() + "\n");
            }
            
            writer.write(cf.getOriginFunction().getName() + ":\n");
            
            if (!cf.prologue.isEmpty()) {
                for (AbstractMachineCode mc : cf.prologue)
                    emitMC(writer, mc);
            }
            
            for (AbstractMachineCode mc : cf.finalMC) {
                emitMC(writer, mc);
            }
            
            if (!cf.epilogue.isEmpty()) {
                for (AbstractMachineCode mc : cf.epilogue)
                    emitMC(writer, mc);
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
    
    private static void emitMC(BufferedWriter writer, AbstractMachineCode mc) throws IOException {
        if (mc.getLabel() != null) {
            writer.write(UVMCompiler.MCDriver.emitOp(mc.getLabel()) + ":");
            writer.write('\n');
        }
        writer.write('\t');
        writer.write(mc.emit());
        writer.write('\n');
    }
}
