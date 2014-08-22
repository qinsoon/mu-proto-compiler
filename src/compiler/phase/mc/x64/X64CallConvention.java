package compiler.phase.mc.x64;

import java.util.List;
import java.util.ArrayList;

import burm.mc.X64add;
import burm.mc.X64pop;
import burm.mc.X64push;
import compiler.UVMCompiler;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;
import uvm.inst.InstCall;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;

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
            if (returnType.fitsInFPR() == 1) {
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
    
    int argumentsSizeOnStack = -1;
    List<MCRegister> callerSavedRegs = new ArrayList<MCRegister>();     // push order
    
    public List<AbstractMachineCode> callerSetupCallSequence(
            CompiledFunction caller, 
            Function callee,
            AbstractMachineCode callMC) {
        List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
        
        // caller saved registers
        callerSavedRegs.clear();
        int callMCIndex = callMC.sequence;
        System.out.println("setup call seq at " + callMCIndex);
        caller.printInterval();
        List<MCRegister> liveRegs = caller.getLiveRegistersAt(callMCIndex);
        // add live-in regs
        MCBasicBlock callBB = caller.getBasicBlockFor(callMC);
        for (MCRegister reg : callBB.liveIn)
            if (!liveRegs.contains(reg.REP()))
                liveRegs.add(reg.REP());

        for (MCRegister reg : liveRegs) {
            System.out.println("Pushing " + reg.prettyPrint() + ", hash:" + reg.hashCode());
            ret.add(pushStack(reg));
            callerSavedRegs.add(reg);
        }
        
        // deal with arguments
        InstCall callIR = (InstCall) callMC.getHighLevelIR();
        List<Value> args = callIR.getArguments();
        List<Type> argTypes = callee.getSig().getParamTypes();
        
        UVMCompiler._assert(
                argTypes.size() == args.size(), 
                "call IR has " + args.size() + " arguments while func signature has " + argTypes.size());
        
        int usedParamGPRs = 0;
        int usedParamFPRs = 0;
        argumentsSizeOnStack = 0;
        
        for (int i = 0; i < argTypes.size(); i++) {
            Type curType = argTypes.get(i);
            MCOperand arg = operandFromNode(caller, args.get(i));
            if (curType.fitsInGPR() > 0) {
                if (curType.fitsInGPR() == 1) {                   
                    if (usedParamGPRs < UVMCompiler.MCDriver.getNumberOfGPRParam()) {
                        // pass by register
                        MCRegister nextAvailParamReg = caller.findOrCreateRegister(
                                UVMCompiler.MCDriver.getGPRParamName(usedParamGPRs),
                                MCRegister.MACHINE_REG,
                                MCRegister.DATA_GPR);
                        
                        ret.add(UVMCompiler.MCDriver.genMove(nextAvailParamReg, arg));
                        
                        usedParamGPRs++;
                    } else {
                        // pass by stack
                        ret.add(pushStack(arg));
                        argumentsSizeOnStack += UVMCompiler.MC_REG_SIZE_IN_BYTES;
                    }
                } else {
                    UVMCompiler.error("argument requires more than one GPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() > 0){
                if (curType.fitsInFPR() == 1) {
                    if (usedParamFPRs < UVMCompiler.MCDriver.getNumberOfFPRParam()) {
                        // pass by register
                        MCRegister nextAvailParamReg = caller.findOrCreateRegister(
                                UVMCompiler.MCDriver.getFPRParamName(usedParamFPRs), 
                                MCRegister.MACHINE_REG, 
                                MCRegister.DATA_DP);
                        
                        ret.add(UVMCompiler.MCDriver.genDPMove(nextAvailParamReg, arg));
                        
                        usedParamFPRs++;
                    } else {
                        // pass by stack
                        ret.add(pushStack(arg));
                        argumentsSizeOnStack += UVMCompiler.MC_FP_REG_SIZE_IN_BYTES;
                    }
                } else {
                    UVMCompiler.error("argument requires more than one FPR, unimplemented. ");
                }
            } else if (curType.fitsInFPR() == 0 && curType.fitsInGPR() == 0) {
                UVMCompiler.error("a param doesnt fit in registers, and passed on stack. unimplemented");
            } else {
                UVMCompiler.error("Type " + curType.prettyPrint() + " seems errornous on its fitness of registers. ");
            }
        }
        
        // push return address - dont need to do this
//        MCLabel callerLabel = (MCLabel) operandFromNode(caller, caller.getOriginFunction().getFuncLabel());
//        MCMemoryOperand callerRelAddress = new MCMemoryOperand();
//        MCRegister rip = caller.findOrCreateRegister(UVMCompiler.MCDriver.getInstPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
//        callerRelAddress.setBase(rip);
//        callerRelAddress.setDispLabel(callerLabel);
//        ret.add(pushStack(callerRelAddress));
        
        // generate call
        // FIXME: why generate a new call inst instead of using the old one?
        ret.add(callMC);
//        ret.add(UVMCompiler.MCDriver.genCall((MCLabel) operandFromNode(caller, callee.getFuncLabel())));
        
        return ret;
    }
    
    public List<AbstractMachineCode> callerCleanupCallSequence(
            CompiledFunction caller, 
            Function callee,
            AbstractMachineCode callMC) {
        List<AbstractMachineCode> ret = new ArrayList<AbstractMachineCode>();
        
        // trash arguments on stack
        // add rsp, xxx
        if (argumentsSizeOnStack == -1)
            UVMCompiler.error("unknown arguments size on stack: should call callerSetupCallSequence() before call callerCleanupCallSequence()");
        
        if (argumentsSizeOnStack != 0) {
            MCRegister rsp = caller.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
            X64add trashArguments = new X64add();
            trashArguments.setOperand0(rsp);
            trashArguments.setOperand1(new MCIntImmediate(argumentsSizeOnStack));
            trashArguments.setReg(rsp);
            ret.add(trashArguments);
        }
        
        // save return value to designated reg or memory location
        MCRegister retResult = null;
        if (!callMC.getReg().REP().isSpilled())
            retResult = callMC.getReg().REP();
        else {
            UVMCompiler.error("unimplemented: return value is spilled onto stack");
        }
        
        Type returnType = callee.getSig().getReturnType();
        
        if (returnType.fitsInGPR() > 0) {
            if (returnType.fitsInGPR() == 1) {
                MCRegister realRetReg = caller.findOrCreateRegister(UVMCompiler.MCDriver.getGPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
                ret.add(UVMCompiler.MCDriver.genMove(retResult, realRetReg));
            } else {
                UVMCompiler.error("a return value fits in several GPRs, unimplemented");
            }
        } else if (returnType.fitsInFPR() > 0) {
            if (returnType.fitsInFPR() == 1) {
                MCRegister realRetReg = caller.findOrCreateRegister(UVMCompiler.MCDriver.getFPRRetName(0), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
                
                ret.add(UVMCompiler.MCDriver.genDPMove(retResult, realRetReg));
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
        
        // restore caller saved register
        for (int i = callerSavedRegs.size() - 1; i >= 0; i--) {
            ret.add(popStack(callerSavedRegs.get(i)));
        }
        
        return ret;
    }
    
    public void genPrologue(CompiledFunction cf) {
        List<AbstractMachineCode> prologue = cf.prologue;
        
        // set up its own frame
        
        // push rbp
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        AbstractMachineCode pushRBP = pushStack(rbp);
        pushRBP.setLabel(new MCLabel("prologue"));
        prologue.add(pushRBP);
        
        // rsp -> rbp
        MCRegister rsp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        prologue.add(UVMCompiler.MCDriver.genMove(rbp, rsp));
        
        // allocate space for local storage
        int stackDisp = 0;
        for (MCRegister reg : cf.intervals.keySet()) {
            if (cf.intervals.get(reg).hasValidRange() && reg.isSpilled()) {
                if (reg.getDataType() == MCRegister.DATA_GPR) {
                    stackDisp -= UVMCompiler.MC_REG_SIZE_IN_BYTES;
                }
                else if (reg.getDataType() == MCRegister.DATA_DP ||
                        reg.getDataType() == MCRegister.DATA_SP) {
                    UVMCompiler.error("unimplemented: calculate stack disp for spilled fp regs");
                } else {
                    UVMCompiler.error("unimplemented: calculate stack disp");
                }
            }
        }
        if (stackDisp != 0) {
            X64add dispRSP = new X64add();
            dispRSP.setOperand0(rsp);
            dispRSP.setOperand1(new MCIntImmediate(stackDisp));
            dispRSP.setReg(rsp);
            prologue.add(dispRSP);
        }
        
        // callee-saved register
        for (MCRegister reg : cf.intervals.keySet()) {
            Interval li = cf.intervals.get(reg);
            if (UVMCompiler.MCDriver.isCalleeSave(reg.getName()) && li.hasValidRange()) {
                cf.calleeSavedRegs.add(reg);
                // need to save it
                prologue.add(pushStack(reg));
            }
        }
    }
    
    public void genEpilogue(CompiledFunction cf) {
        List<AbstractMachineCode> epilogue = cf.epilogue;
        
        // restore callee-saved regs
        for (int i = cf.calleeSavedRegs.size() - 1; i >= 0; i--) {
            epilogue.add(popStack(cf.calleeSavedRegs.get(i)));
        }
        
        // set rbp -> rsp
        MCRegister rbp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getFramePtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        MCRegister rsp = cf.findOrCreateRegister(UVMCompiler.MCDriver.getStackPtrReg(), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
        epilogue.add(UVMCompiler.MCDriver.genMove(rsp, rbp));
        
        // pop rbp
        epilogue.add(popStack(rbp));
    }
    
    private X64pop popStack(MCOperand dst) {
        X64pop ret = new X64pop();
        ret.setOperand(0, dst);
        return ret;
    }
    
    private X64push pushStack(MCOperand src) {
        X64push ret = new X64push();
        ret.setOperand(0, src);
        return ret;
    }
    
    private static MCOperand operandFromNode(CompiledFunction cf, IRTreeNode node) {
        MCOperand ret;
        switch(node.getOpcode()) {
        case OpCode.INT_IMM:
          ret = new uvm.mc.MCIntImmediate(((uvm.IntImmediate)node).getValue()); break;
        case OpCode.FP_SP_IMM:
          ret = new uvm.mc.MCSPImmediate(((uvm.FPImmediate)node).getFloat()); break;
        case OpCode.FP_DP_IMM:
          ret = new uvm.mc.MCDPImmediate(((uvm.FPImmediate)node).getDouble()); break;
        case OpCode.REG_I1:
        case OpCode.REG_I8:
        case OpCode.REG_I16:
        case OpCode.REG_I32:
        case OpCode.REG_I64:
          ret = cf.findOrCreateRegister(
                  ((uvm.Register)node).getName(), 
                  uvm.mc.MCRegister.OTHER_SYMBOL_REG, 
                  uvm.mc.MCRegister.DATA_GPR); break;
        case OpCode.LABEL:
          ret = new uvm.mc.MCLabel(((uvm.Label)node).getName()); break;
        default:
          ret = cf.findOrCreateRegister(
                  "res_reg"+node.getId(), 
                  uvm.mc.MCRegister.RES_REG, 
                  getDataTypeFromHighLevelIR(node)); break;
        }
        ret.highLevelOp = node;
        return ret;
    }
    
    private static int getDataTypeFromHighLevelIR(IRTreeNode ir) {
        switch(ir.getOpcode()) {
        case OpCode.REG_I1:
        case OpCode.REG_I8:
        case OpCode.REG_I16:
        case OpCode.REG_I32:
        case OpCode.REG_I64:
        case OpCode.INT_IMM:
            return MCRegister.DATA_GPR;
        case OpCode.FP_SP_IMM:
            return MCRegister.DATA_SP;
        case OpCode.FP_DP_IMM:
            return MCRegister.DATA_DP;
        default:
            if (ir instanceof Instruction) {
                Instruction inst = (Instruction) ir;
                return getDataTypeFromHighLevelIR(inst.getDefReg());
            }
            UVMCompiler.error("cannot get data type from HLL IR:" + ir.prettyPrint());
            return -1;
        }
    }
}