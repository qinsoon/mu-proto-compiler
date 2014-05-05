package compiler.phase;

import java.util.List;

import compiler.UVMCompiler;
import burm.BURM_GENERATED;
import uvm.BasicBlock;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCRegister;

public class MCRepresentationGeneration extends CompilationPhase{
    private static final boolean VERBOSE = true;
    
    public MCRepresentationGeneration(String name) {
        super(name);
    }

    public void execute() {
        for (Function f : MicroVM.v.funcs.values()) {
            CompiledFunction cf = new CompiledFunction(f);
            
            for (IRTreeNode node : f.tree) {
                List<AbstractMachineCode> mc = BURM_GENERATED.emitCode(node);
                if (node instanceof Instruction) {
                    if (((Instruction) node).getLabel() != null) {
                        mc.get(0).setLabel(new uvm.mc.MCLabel(((Instruction) node).getLabel().getName()));
                    }
                } else UVMCompiler.error("node " + node.prettyPrint() + " is not an inst");
                
                cf.addMachineCode(mc);
            }
            
            cf.setRegs(MCRegister.temps);
            MCRegister.clearTemps();
            
            if (VERBOSE) {
                System.out.println(f.getName() + " machine code:");
                for (AbstractMachineCode mc : cf.getMachineCode()) {
                    System.out.println(mc.prettyPrint());
                }
                System.out.println();
            }
            
            MicroVM.v.compiledFunc(cf);
        }
    }

    @Override
    protected void preChecklist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void postChecklist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {

    }

    @Override
    protected void visitInstruction(Instruction inst) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitFunction(Function f) {
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }
}
