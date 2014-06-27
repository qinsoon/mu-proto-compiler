package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import burm.BURM_GENERATED;
import burm.BurmState;

public class InstructionSelection extends AbstractCompilationPhase{

    public InstructionSelection(String name, boolean verbose) {
        super(name, verbose);
    }
    
    private void print(IRTreeNode node) {
        verboseln(node.printNode());
        verboseln(node.state.prettyPrint());
        
        for (int i = 0; i < node.getArity(); i++)
            print(node.getChild(i));
    }
    
    public BurmState label(IRTreeNode p) {        
        if (p != null) {
            BurmState[] leaves = new BurmState[p.getArity()];
            for (int i = 0; i < leaves.length; i++)
                leaves[i] = label(p.getChild(i));
            
            p.state = BURM_GENERATED.state(p, leaves);
            return p.state;
        } else
            return null;
    }

    @Override
    protected void preChecklist() {
        verboseln("\nStart instruction selection ... \n");
    }

    @Override
    protected void postChecklist() {
        if (verbose) {
            for (Function f : MicroVM.v.funcs.values()) {
                for (IRTreeNode node : f.tree) {
                    print(node);
                    verboseln();
                }
            }
        }
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        label(node);
    }

    @Override
    protected void visitInstruction(Instruction inst) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitFunction(Function f) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }
}
