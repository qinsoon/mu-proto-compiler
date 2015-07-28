package compiler.phase;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.Register;
import uvm.Value;
import uvm.inst.AbstractCall;

public class DefUseGeneration extends AbstractCompilationPhase{

    public DefUseGeneration(String name, boolean verbose) {
        super(name, verbose);
    }

    @Override
    protected void visitInstruction(Instruction inst) {
        verboseln("generating def-use for inst:" + inst.prettyPrint());
        // uses
        for (Value v : inst.getOperands()) {
            if (v.isRegister()) {
                Register reg = (Register) v;
                inst.getRegUses().add(reg);
                reg.addUse(inst);
                
                verboseln("  uses:" + v.prettyPrint());
            }
        }
//        
//        // arg as uses
//        if (inst instanceof AbstractCall) {
//        	for (Value v : ((AbstractCall) inst).getArguments()) 
//        		if (v.isRegister()) {
//        			Register reg = (Register) v;
//        			inst.getRegUses().add(reg);
//        			reg.addUse(inst);
//        		}
//        }
        
        // def
        if (inst.getDefReg() != null) {
            inst.getDefReg().setDef(inst);
            verboseln("  def:" + inst.getDefReg().prettyPrint());
        }
    }

    @Override
    protected void visitFunction(Function f) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void preChecklist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void postChecklist() {
        verboseln();
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        // TODO Auto-generated method stub
        
    }
}
