package compiler;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Register;
import uvm.Value;
import uvm.inst.InstPseudoAssign;

public class IRTreeGeneration {
    public static void execute() {
        for (Function f : MicroVM.v.funcs.values()) {
            for (BasicBlock bb : f.getBBs()) {
                for (Instruction inst : bb.getInsts()) {
                    
                    for (Value v : inst.getOperands()) {
                        inst.addChild(v);
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
                printNode(1, node);
            }
        }
    }
    
    public static void printNode(int level, IRTreeNode node) {
//        for (int i = 0; i < level; i++)
//            System.out.print(' ');
        System.out.println("+" + node.prettyPrint());
        for (int i = 0; i < node.getArity(); i++) {
//            printNode(level + 1, node.getChild(i));
        }
            
    }
}
