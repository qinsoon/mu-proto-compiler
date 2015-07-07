package compiler.phase.mc;

import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;

public class RetainHighLevelDataType extends AbstractMCCompilationPhase {

    public RetainHighLevelDataType(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        for (AbstractMachineCode mc : cf.getMachineCode()) {
            for (int i = 0; i < mc.getNumberOfOperands(); i++) {
                MCOperand op = mc.getOperand(i);
                
                if (op instanceof MCRegister) {
                    int dataType = ((MCRegister) op).getDataType();
                    if (dataType == 0 || dataType == -1) {
                        ((MCRegister) op).setDataType(getDataTypeFromHighLevelOp(op.highLevelOp));
                    }
                }
            }
        }
    }

    private int getDataTypeFromHighLevelOp(IRTreeNode op) {
        // we try restore data type from high level op code
        switch (op.getOpcode()) {
        case OpCode.REG_I1:
        case OpCode.REG_I8:
        case OpCode.REG_I16:
        case OpCode.REG_I32:
        case OpCode.REG_I64:
            return MCRegister.DATA_GPR;
        case OpCode.FP_DP_IMM:
            return MCRegister.DATA_DP;
        case OpCode.FP_SP_IMM:
            return MCRegister.DATA_SP;
        default:
            if (op instanceof Instruction) {
                return getDataTypeFromHighLevelOp(((Instruction)op).getDefReg());
            } else {
                System.out.println("Trying to restore reg data type from high level op code");
                System.out.println("HLL OP = " + op.prettyPrint());
                UVMCompiler.error("This is not a register op");
                return -1;
            }
        }
    }
}
