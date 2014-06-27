package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;

public abstract class AbstractCompilationPhase {
    protected String name;
    protected final boolean verbose;
    
    public AbstractCompilationPhase(String name, boolean verbose) {
        this.name = name;
        this.verbose = verbose;
    }
    
    public void verboseln(Object o) {
        if (verbose)
            System.out.println(o);
    }
    
    public void verbose(Object o) {
        if (verbose)
            System.out.print(o);
    }
    
    public void verboseln() {
        if (verbose)
            System.out.println();
    }
    
    public void execute() {
        verboseln("=========== " + name + " ===========\n");
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
