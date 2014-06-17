package compiler.phase.mc.x64;

import java.util.List;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.Type;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCRegister;

public class X64CallConvention {
    public void calleeInitParameterRegisters(CompiledFunction cf) {
        // set param_regi to the ith of param registers if suitable
        
        int usedGPRParamReg = 0;
        int usedFPRParamReg = 0;
        
        List<Type> paramTypes = cf.getOriginFunction().getSig().getParamTypes();
        for (int i = 0; 
                i < paramTypes.size() && 
                (usedGPRParamReg < UVMCompiler.MCDriver.getNumberOfGPRParam() || usedFPRParamReg < UVMCompiler.MCDriver.getNumberOfFPRParam());
                i++) {
            Type paramType = paramTypes.get(i);
            if (paramType.fitsInGPR() > 0) {
                if (paramTypes.get(i).fitsInGPR() == 1) {
                    // we need to set param_regi to the ith of GPR Param
                    MCRegister symbolParamReg = cf.findRegister("param_reg"+i, MCRegister.PARAM_REG);
                    MCRegister realParamReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRParamName(usedGPRParamReg), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                    usedGPRParamReg++;
                    
                    symbolParamReg.setREP(realParamReg);
                } else {
                    // fits in several GPRs
                    UVMCompiler.error("a param fits in several GPRs, unimplemented");
                }
            } else if (paramType.fitsInFPR() > 0) {
                if (paramTypes.get(i).fitsInFPR() == 1) {
                    // double precision
                    MCRegister symbolParamReg = cf.findRegister("param_reg"+i, MCRegister.PARAM_REG);
                    MCRegister realParamReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFPRParamName(usedFPRParamReg), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                    usedFPRParamReg++;
                    
                    symbolParamReg.setREP(realParamReg);
                } else {
                    UVMCompiler.error("a param fits in single-precision FPRs, unimplemented");                    
                }
            } else if (paramType.fitsInGPR() == 0 && paramType.fitsInFPR() == 0) {
                UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + paramType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
    }
    
    public void calleeInitReturnRegister(CompiledFunction cf) {
        // set ret_regi to the ith of return registers
        
        Type returnType = cf.getOriginFunction().getSig().getReturnType();
        
        if (returnType.fitsInGPR() > 0) {
            if (returnType.fitsInGPR() == 1) {
                // set ret_reg0 to the 0th of return register
                MCRegister symbolRetReg = cf.findRegister("ret_reg0", MCRegister.RET_REG);
                MCRegister realRetReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getGPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                
                symbolRetReg.setREP(realRetReg);
            } else {
                UVMCompiler.error("a return value fits in several GPRs, unimplemented");
            }
        } else if (returnType.fitsInFPR() > 0) {
            if (returnType.fitsInFPR() == 2) {
                MCRegister symbolRetReg = cf.findRegister("ret_reg0", MCRegister.RET_REG);
                MCRegister realRetReg = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                
                symbolRetReg.setREP(realRetReg);
            } else {
                UVMCompiler.error("a return value fits in single-precision FPRs, unimplemented");
            }
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
    
    public List<AbstractMachineCode> callerSetupCallSequence(
            CompiledFunction caller, 
            Function callee,
            AbstractMachineCode callMC) {
        return null;
    }
}
