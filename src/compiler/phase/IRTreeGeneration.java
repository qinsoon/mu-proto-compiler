package compiler.phase;

import java.util.Map.Entry;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.IntImmediate;
import uvm.Label;
import uvm.MicroVM;
import uvm.OpCode;
import uvm.Register;
import uvm.Value;
import uvm.inst.*;
import uvm.type.Int;

public class IRTreeGeneration extends AbstractCompilationPhase{
    public IRTreeGeneration(String name, boolean verbose) {
        super(name, verbose);
    }
    
    private static void checkAndAddValue(Instruction inst, Value v) {
        if (v instanceof Register) {
            Register reg = (Register) v;
            if (reg.usesOnlyOnce()) {
                // merging an inst with another
                Instruction merged = reg.getDef();
                inst.addChild(reg.getDef());                
                
                if (merged.getLabel() != null) {
                    inst.setLabel(merged.getLabel());
                    merged.setLabel(null);
                }
            }
            else inst.addChild(v);
        } else inst.addChild(v);
    }
    
    public void printIRTree() {
        for (Function f : MicroVM.v.funcs.values()) {
            verboseln(f);
            
            for (IRTreeNode node : f.tree) {
                verboseln("+" + node.printNode());
            }
        }
    }

    @Override
    protected void preChecklist() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void postChecklist() {
       if (verbose)
           printIRTree();
    }

    @Override
    protected void visitInstruction(Instruction inst) {
        if (inst instanceof InstBranch)
            inst.addChild(((InstBranch)inst).getTarget());
        else if (inst instanceof InstBranch2) {
            checkAndAddValue(inst, ((InstBranch2) inst).getCond());
            inst.addChild(((InstBranch2) inst).getIfTrue());
            inst.addChild(((InstBranch2) inst).getIfFalse());;
        } else if (inst instanceof InstPhi) {
            InstPhi phi = (InstPhi) inst;
            
            for (Entry<Label, Value> entry : phi.getValues().entrySet()) {
                checkAndAddValue(phi, entry.getValue());
                inst.addChild(entry.getKey());
            }
        } else if (inst instanceof InstCall) {
            InstCall call = (InstCall) inst;
            
            inst.addChild(call.getCallee().getFuncLabel());
        } else if (inst instanceof InstCCall) {
        	InstCCall ccall = (InstCCall) inst;
        	
        	inst.addChild(MicroVM.v.findOrCreateGlobalLabel(ccall.getFunc()));
        } else if (inst instanceof InstAlloca) {
        	InstAlloca alloca = (InstAlloca) inst;
        	
        	inst.addChild(new IntImmediate(Int.I64, (long) alloca.getType().alignmentInBytes()));
        }
        
        else {
            for (Value v : inst.getOperands()) {
                checkAndAddValue(inst, v);
            }
        }
        
        if (inst.hasDefReg()) {
            // we dont need to define it
            // it becomes a subtree of another node
            if (inst.getDefReg().usesOnlyOnce()) {
                return;
            }
            
            Instruction assign = new InstPseudoAssign(inst.getDefReg(), inst);
            assign.addChild(inst.getDefReg());
            assign.addChild(inst);
            
            // check if the old inst has a label associated with it
            if (inst.getLabel() != null) {
                assign.setLabel(inst.getLabel());
                inst.setLabel(null);
            }                            
            
            f.tree.add(assign);
        } else {
            f.tree.add(inst);
        }
    }
    
    Function f;
    @Override
    protected void visitFunction(Function f) {
        this.f = f;
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        // TODO Auto-generated method stub
        
    }
}
