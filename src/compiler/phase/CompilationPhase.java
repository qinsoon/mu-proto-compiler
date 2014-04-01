package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;

public abstract class CompilationPhase {
    String name;
    
    public CompilationPhase(String name) {
        this.name = name;
    }
    
    public void execute() {
        preChecklist();
        
        for (Function f : MicroVM.v.funcs.values()) {
            visitFunction(f);
            
            for (BasicBlock bb : f.getBBs()) {
                visitBasicBlock(bb);
                
                for (Instruction inst : bb.getInsts()) {
                    visitInstruction(inst);
                }
            }
            
            for (IRTreeNode node : f.tree) 
                visitTreeNode(node);
        }
        
        postChecklist();
    }
    
    protected void preChecklist() {}
    protected void postChecklist() {}
    
    protected void visitTreeNode(IRTreeNode node) {}
    protected void visitInstruction(Instruction inst) {}
    protected void visitFunction(Function f) {}    
    protected void visitBasicBlock(BasicBlock bb) {}
}
