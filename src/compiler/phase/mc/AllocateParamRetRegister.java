package compiler.phase.mc;

import java.util.List;

import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.Type;
import uvm.mc.MCRegister;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;

public class AllocateParamRetRegister extends AbstractMCCompilationPhase{

    public AllocateParamRetRegister(String name) {
        super(name);
    }
    
    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        // set param_regi to the ith of param registers if suitable
        
        int usedGPRParamReg = 0;
        List<Type> paramTypes = cf.getOriginFunction().getSig().getParamTypes();
        for (int i = 0; i < paramTypes.size() && usedGPRParamReg < UVMCompiler.MCDriver.getNumberOfGPRParam(); i++) {
            Type paramType = paramTypes.get(i);
            if (paramType.fitsInGPR() > 0) {
                if (paramTypes.get(i).fitsInGPR() == 1) {
                    // we need to set param_regi to the ith of GPR Param
                    MCRegister symbolParamReg = cf.findRegister("param_reg"+i, MCRegister.PARAM_REG);
                    MCRegister realParamReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRParamName(usedGPRParamReg), MCRegister.MACHINE_REG);
                    usedGPRParamReg++;
                    
                    symbolParamReg.setREP(realParamReg);
                } else {
                    // fits in several GPRs
                    UVMCompiler.error("a param fits in several GPRs, unimplemented");
                }
            } else if (paramType.fitsInFPR() > 0) {
                UVMCompiler.error("a param fits in FPRs, unimplemented");
            } else if (paramType.fitsInGPR() == 0 && paramType.fitsInFPR() == 0) {
                UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + paramType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
        
        // set ret_regi to the ith of return registers
        
        Type returnType = cf.getOriginFunction().getSig().getReturnType();
        
        if (returnType.fitsInGPR() > 0) {
            if (returnType.fitsInGPR() == 1) {
                // set ret_reg0 to the 0th of return register
                MCRegister symbolRetReg = cf.findRegister("ret_reg0", MCRegister.RET_REG);
                MCRegister realRetReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRRetName(0), MCRegister.MACHINE_REG);
                
                symbolRetReg.setREP(realRetReg);
            } else {
                UVMCompiler.error("a return value fits in several GPRs, unimplemented");
            }
        } else if (returnType.fitsInFPR() > 0) {
            UVMCompiler.error("a return value fits in FPRs, unimplemented");
        } else if (returnType.fitsInGPR() == 0 && returnType.fitsInFPR() == 0) {
            if (returnType instanceof uvm.type.Void) {
                // we dont need to do anything
            } else { 
                UVMCompiler.error("a return value of Type " + returnType.prettyPrint() + " doesnt fit in registers, and passed on stack. unimplemented");
            }
        } else {
            UVMCompiler.error("Type " + returnType.prettyPrint() + " seems errornous on its fitness of registers. ");
        }
    }

}
