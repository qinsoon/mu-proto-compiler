package compiler;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.OpCode;
import uvm.Register;
import uvm.Value;
import uvm.inst.*;

public class IRTreeGeneration {
    public static void execute() {
        for (Function f : MicroVM.v.funcs.values()) {
            for (BasicBlock bb : f.getBBs()) {
                for (Instruction inst : bb.getInsts()) {
                    
                    if (inst instanceof InstBranch)
                        inst.addChild(((InstBranch)inst).getTarget());
                    else if (inst instanceof InstBranch2) {
                        inst.addChild(((InstBranch2) inst).getCond());
                        inst.addChild(((InstBranch2) inst).getIfTrue());
                        inst.addChild(((InstBranch2) inst).getIfFalse());;
                    } else if (inst instanceof InstPhi) {
                        inst.addChild(((InstPhi) inst).getVal1());
                        inst.addChild(((InstPhi) inst).getLabel1());
                        inst.addChild(((InstPhi) inst).getVal2());
                        inst.addChild(((InstPhi) inst).getLabel2());
                    } else {
                        for (Value v : inst.getOperands()) {
                            inst.addChild(v);
                        }
                    }
                    
                    if (inst.hasDefReg()) {
                        Instruction assign = new InstPseudoAssign(inst.getDefReg(), inst);
                        assign.addChild(inst.getDefReg());
                        assign.addChild(inst);
                        
                        f.tree.add(assign);
                    } else {
                        f.tree.add(inst);
                    }
                }
            }
        }
        
        printIRTree();
    }
    
    public static void printIRTree() {
        for (Function f : MicroVM.v.funcs.values()) {
            System.out.println(f);
            
            for (IRTreeNode node : f.tree) {
                System.out.println("+" + node.printNode());
            }
        }
    }
}
