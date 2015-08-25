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
import uvm.runtime.RuntimeFunction;
import uvm.type.Int;

public class IRTreeGeneration extends AbstractCompilationPhase{
    public IRTreeGeneration(String name, boolean verbose) {
        super(name, verbose);
    }
    
    // do not move load/store which may break dependency
    // do not move PARAM (it may eliminates param_reg)
    private static boolean isMovable(Instruction inst) {
    	return !(inst instanceof AbstractLoad || inst instanceof InstStore || inst instanceof InstParam);
    }
    
    private static void checkAndAddValue(Instruction inst, Value v) {
        if (v instanceof Register) {
            Register reg = (Register) v;
            if (reg.usesOnlyOnce() && isMovable(reg.getDef())) {
                // merging an inst with another
                Instruction merged = reg.getDef();
                inst.addChild(reg.getDef());                
                
//                if (merged.getLabel() != null) {
//                    inst.setLabel(merged.getLabel());
//                    merged.setLabel(null);
//                }
            }
            else inst.addChild(v);
        } else inst.addChild(v);
    }
    
    public void printIRTree() {
        for (Function f : MicroVM.v.funcs.values()) {
            verboseln(f);
            
            for (IRTreeNode node : f.tree) {
            	if (node instanceof Instruction) {
            		Instruction inst = (Instruction) node;
            		if (inst.getLabel() != null)
            			verboseln("#" + inst.getLabel().getName() + ":");            		
            	}

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
    	// remove the label (we put label when we finish a basic block)
    	if (inst.getLabel() != null)
    		inst.setLabel(null);
    	
    	Instruction addedInst = TranslateIntoTreeNode(inst);
    	if (addedInst != null && firstInBB == null)
    		firstInBB = addedInst;
        
        verboseln(instIndex + " of " + curBB.getInsts().size() + " insts in the BB");
        verboseln(inst.prettyPrint());
        if (instIndex == curBB.getInsts().size() - 1) {
        	// we finished this BB
        	firstInBB.setLabel(curBB.getLabel());
        	
        	verboseln("adding label#" + curBB.getLabel().getName() + " to " + firstInBB.prettyPrint());
        } else {
            instIndex++;
        }
    }

	private Instruction TranslateIntoTreeNode(Instruction inst) {
		/*
    	 * BRANCH
    	 */
        if (inst instanceof InstBranch)
            inst.addChild(((InstBranch)inst).getTarget());
        else if (inst instanceof InstBranch2) {
            checkAndAddValue(inst, ((InstBranch2) inst).getCond());
            inst.addChild(((InstBranch2) inst).getIfTrue());
            inst.addChild(((InstBranch2) inst).getIfFalse());;
        } 
        /*
         * PHI
         */
//        else if (inst instanceof InstPhi) {
//            InstPhi phi = (InstPhi) inst;
//            
//            for (Entry<Label, Value> entry : phi.getValues().entrySet()) {
//                checkAndAddValue(phi, entry.getValue());
//                inst.addChild(entry.getKey());
//            }
//        } 
        /*
         * CALL
         */
        else if (inst instanceof InstCall) {
            InstCall call = (InstCall) inst;
            
            inst.addChild(call.getCallee().getFuncLabel());
        } else if (inst instanceof InstCCall) {
        	InstCCall ccall = (InstCCall) inst;
        	
        	inst.addChild(MicroVM.v.findOrCreateGlobalLabel(ccall.getFunc()));
        } 
        /*
         * MEMORY ALLOCATION
         */
        else if (inst instanceof InstAlloca) {
        	InstAlloca alloca = (InstAlloca) inst;
        	
        	inst.addChild(new IntImmediate(Int.I64, (long) alloca.getType().alignmentInBytes()));
        }
        /*
         * MEMORY ACCESS
         */
        else if (inst instanceof InstGetFieldIRef) {
        	InstGetFieldIRef getField = (InstGetFieldIRef) inst;
        	
        	inst.addChild(getField.getLoc());
        	inst.addChild(new IntImmediate(Int.I64, MicroVM.v.objectModel.getOffsetFromStructIRef(getField.getStructType(), getField.getIndex())));
        }
        else if (inst instanceof InstGetIRef) {
        	InstGetIRef getIRef = (InstGetIRef) inst;
        	
        	int headerSize = MicroVM.v.objectModel.getHeaderSize(getIRef.getReferentType());
        	
        	inst.addChild(getIRef.getRef());
        	inst.addChild(new IntImmediate(Int.I64, (long) headerSize));
        }
        else if (inst instanceof InstGetElemIRefConstIndex) {
        	InstGetElemIRefConstIndex getElem = (InstGetElemIRefConstIndex) inst;
        	
        	inst.addChild(getElem.getLoc());
        	inst.addChild(new IntImmediate(Int.I64, MicroVM.v.objectModel.getOffsetFromArrayIRef(getElem.getArrayType(), (int) getElem.getIndex())));
        }
        else if (inst instanceof InstGetElemIRefVarIndex) {
        	InstGetElemIRefVarIndex getElem = (InstGetElemIRefVarIndex) inst;
        	
        	inst.addChild(getElem.getLoc());
        	inst.addChild(getElem.getIndex());
        	inst.addChild(new IntImmediate(Int.I64, getElem.getArrayType().getEleType().sizeInBytes()));
        }
        /*
         * DEFAULT: add all operands as children
         */
        else {
            for (Value v : inst.getOperands()) {
                checkAndAddValue(inst, v);
            }
        }
        
        if (inst.hasDefReg()) {
            // we dont need to define it
            // it becomes a subtree of another node
            if (inst.getDefReg().usesOnlyOnce() && inst.getDefReg().hasUsesNotAsArgument() && isMovable(inst)) {
                return null;
            }
            
            Instruction assign = new InstPseudoAssign(inst.getDefReg(), inst);
            assign.addChild(inst.getDefReg());
            assign.addChild(inst);
            
            // check if the old inst has a label associated with it
//            if (inst.getLabel() != null) {
//                assign.setLabel(inst.getLabel());
//                inst.setLabel(null);
//            }                            
            
            f.tree.add(assign);
            return assign;
        } else {
            f.tree.add(inst);
            return inst;
        }
	}
    
    Function f;
    BasicBlock curBB;
    int instIndex;
    Instruction firstInBB;
    
    @Override
    protected void visitFunction(Function f) {
        this.f = f;
    }

    @Override
    protected void visitBasicBlock(BasicBlock bb) {
        instIndex = 0;
        curBB = bb;
        firstInBB = null;
        
        verboseln("Generating tree for BB " + bb.getName() + "...");
    }

    @Override
    protected void visitTreeNode(IRTreeNode node) {
        // TODO Auto-generated method stub
        
    }
}
