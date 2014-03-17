package compiler;

import uvm.BasicBlock;
import uvm.Function;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Register;
import uvm.Value;

public class DefUseGeneration {
    public static void execute() {
        for (Function f : MicroVM.v.funcs.values()) {
            for (BasicBlock bb : f.getBBs()) {
                for (Instruction inst : bb.getInsts()) {
                    System.out.println("generating def-use for inst:" + inst.prettyPrint());
                    // uses
                    for (Value v : inst.getOperands()) {
                        if (v.isRegister()) {
                            Register reg = (Register) v;
                            inst.getRegUses().add(reg);
                            reg.addUse(inst);
                            
                            System.out.println("  uses:" + v.prettyPrint());
                        }
                    }
                    
                    // def
                    if (inst.getDefReg() != null) {
                        inst.getDefReg().setDef(inst);
                        System.out.println("  def:" + inst.getDefReg().prettyPrint());
                    }
                }
            }
        }
        System.out.println();
    }
}