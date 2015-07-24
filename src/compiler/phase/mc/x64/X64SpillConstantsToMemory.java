package compiler.phase.mc.x64;

import uvm.CompiledFunction;
import uvm.mc.*;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;

public class X64SpillConstantsToMemory extends AbstractMCCompilationPhase {
	static int tempIndex = 0;
	
    public X64SpillConstantsToMemory(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        verboseln("----- After spilling constants ------");
        
        String fName = cf.getOriginFunction().getName();
        
        for (int mi = 0; mi < cf.getMachineCode().size(); mi++) {
        	AbstractMachineCode mc = cf.getMachineCode().get(mi);
            for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                MCOperand operand = mc.getOperand(i);
                if (operand instanceof MCIntImmediate) {
                    if (((MCIntImmediate) operand).getValue() > Integer.MAX_VALUE) {
                    	long[] v = new long[1];
                    	v[0] = ((MCIntImmediate) operand).getValue();
                    	
                    	MCConstant constant = MCConstant.findOrCreateConstant(fName + "_INTCONSTANT", v);
                    	
                    	MCLabeledMemoryOperand memOp = new MCLabeledMemoryOperand();
                        memOp.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
                        memOp.setDispLabel(constant.getLabel());
                        memOp.setSize((byte) 8);
                        
                        // we may end up have two mem Op in an instruction, so we move the constant into a temperory
                        // and let the rest to register allocator
                        MCRegister tempForConst = cf.findOrCreateRegister("tempConst" + (tempIndex++), MCRegister.OTHER_SYMBOL_REG, MCRegister.DATA_GPR);
                        AbstractMachineCode mov = UVMCompiler.MCDriver.genMove(tempForConst, memOp);
                        cf.getMachineCode().add(mi, mov);
                        
                        mc.setOperand(i, tempForConst);
                        
                        verboseln(mc.prettyPrintOneline());
                    }
                } else if (operand instanceof MCDPImmediate) {
                    double value = ((MCDPImmediate) operand).getValue();
                    long[] output = new long[1];
                    output[0] = Double.doubleToRawLongBits(value);
                    
                    // this will be a constant
                    MCConstant constant = MCConstant.findOrCreateConstant(fName + "_DPCONSTANT", output);
                    
                    // this constant will be referred to as a memory op
                    MCLabeledMemoryOperand memOp = new MCLabeledMemoryOperand();
                    memOp.setBase(cf.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR));
                    memOp.setDispLabel(constant.getLabel());
                    memOp.setSize((byte) 8);
                    
                    // replace old immediate operand with new memory operand
                    mc.setOperand(i, memOp);
                    
                    verboseln(mc.prettyPrintOneline());
                } else if (operand instanceof MCSPImmediate) {
                    UVMCompiler.error("single-precision fp constant, unimplemented. ");
                }
            }
        }
    }
}
