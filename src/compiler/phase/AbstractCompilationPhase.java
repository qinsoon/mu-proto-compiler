package compiler.phase;

import compiler.UVMCompiler;
import compiler.util.Pair;
import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;

public abstract class AbstractCompilationPhase {
    protected String name;
    protected final boolean verbose;
    
    protected long start;
    protected long end;
    
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
    	if (UVMCompiler.TIMING_COMPILATION)
    		recordStart();
    	
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
        
        if (UVMCompiler.TIMING_COMPILATION)
        	recordEnd();
    }
    
    protected final void recordStart() {
    	start = System.currentTimeMillis();
    }
    
    protected final void recordEnd() {
    	end = System.currentTimeMillis();
    	UVMCompiler.ELAPSE_TIME.add(new Pair<String, Long>(name, end - start));
    }
    
    protected void preChecklist() {}
    protected void postChecklist() {}
    
    protected void visitTreeNode(IRTreeNode node) {}
    protected void visitInstruction(Instruction inst) {}
    protected void visitFunction(Function f) {}    
    protected void visitBasicBlock(BasicBlock bb) {}
}
