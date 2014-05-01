package compiler.phase.mc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import burm.mc.X64Driver;
import uvm.MicroVM;
import compiler.UVMCompiler;
import compiler.phase.CompilationPhase;
import uvm.*;
import uvm.mc.*;

public class CodeEmission extends CompilationPhase {
    String dir;
    
    public CodeEmission(String name, String dir) {
        super(name);
        this.dir = dir;
    }
    
    @Override
    public void execute() {        
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            BufferedWriter writer = null;
            String fileName = dir + "/" + cf.getOriginFunction().getName() + ".s"; 
            try {
                File outFile = new File(fileName);
                outFile.getParentFile().mkdirs();
                writer = new BufferedWriter(new FileWriter(outFile));
                
                for (AbstractMachineCode mc : cf.finalMC) {
                    if (mc.getLabel() != null) {
                        writer.write(X64Driver.emitOp(mc.getLabel()) + ":");
                        writer.write('\n');
                    }
                    writer.write('\t');
                    writer.write(mc.emit());
                    writer.write('\n');
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
    }
}
