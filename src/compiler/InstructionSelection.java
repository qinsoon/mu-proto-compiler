package compiler;

import uvm.Function;
import uvm.IRTreeNode;
import uvm.MicroVM;
import burm.BURM_GENERATED;
import burm.BurmState;

public class InstructionSelection {
    public static void execute() {
        System.out.println("\nStart instruction selection ... \n");
        
        for (Function f : MicroVM.v.funcs.values()) {
            for (IRTreeNode node : f.tree) {
                label(node);
            }
        }
        
        for (Function f : MicroVM.v.funcs.values()) {
            for (IRTreeNode node : f.tree) {
                print(node);
                System.out.println();
            }
        }
    }
    
    private static void print(IRTreeNode node) {
        System.out.println(node.printNode());
        System.out.println(node.state.prettyPrint());
        
        for (int i = 0; i < node.getArity(); i++)
            print(node.getChild(i));
    }
    
    public static BurmState label(IRTreeNode p) {        
        if (p != null) {
            BurmState[] leaves = new BurmState[p.getArity()];
            for (int i = 0; i < leaves.length; i++)
                leaves[i] = label(p.getChild(i));
            
            p.state = BURM_GENERATED.state(p, leaves);
            return p.state;
        } else
            return null;
    }
}
