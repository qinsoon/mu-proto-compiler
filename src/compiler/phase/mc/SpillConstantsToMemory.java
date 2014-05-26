package compiler.phase.mc;

import uvm.CompiledFunction;
import uvm.mc.*;
import compiler.UVMCompiler;

public class SpillConstantsToMemory extends AbstractMCCompilationPhase {

    public SpillConstantsToMemory(String name) {
        super(name);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        System.out.println("========After spilling constants=========");
        
        String fName = cf.getOriginFunction().getName();
        
        for (AbstractMachineCode mc : cf.finalMC) {
            for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                MCOperand operand = mc.getOperand(i);
                if (operand instanceof MCIntImmediate) {
                    if (((MCIntImmediate) operand).getValue() > Integer.MAX_VALUE) {
                        UVMCompiler.error("large int immedate, need to put to memory. unimplemented");
                    }
                } else if (operand instanceof MCDPImmediate) {
                    double value = ((MCDPImmediate) operand).getValue();
                    long[] output = new long[1];
                    output[0] = Double.doubleToRawLongBits(value);
                    
                    // this will be a constant
                    MCConstant constant = MCConstant.findOrCreateConstant(fName + "_DPCONSTANT", output);
                    
                    // this constant will be referred to as a memory op
                    MCMemoryOperand memOp = new MCMemoryOperand();
                    memOp.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
                    memOp.setDispLabel(constant.getLabel());
                    memOp.setSize((byte) 8);
                    
                    // replace old immediate operand with new memory operand
                    mc.setOperand(i, memOp);
                    
                    System.out.println(mc.prettyPrintOneline());
                } else if (operand instanceof MCSPImmediate) {
                    UVMCompiler.error("single-precision fp constant, unimplemented. ");
                }
            }
        }
    }
}
