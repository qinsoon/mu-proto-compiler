package uvm;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    Label label;
    List<Instruction> insts;
    Function f;
    
    List<BasicBlock> successors 	= new ArrayList<BasicBlock>();
    List<BasicBlock> predecessors 	= new ArrayList<BasicBlock>();
    List<BasicBlock> backEdges		= new ArrayList<BasicBlock>();
    
    public BasicBlock(Function f, String name) {
    	this.f = f;
        this.label = f.findOrCreateLabel(name);
        this.insts = new ArrayList<Instruction>();
    }
    
    public void addInstruction(Instruction i) {
        this.insts.add(i);
    }
    
    public void setInstructions(List<Instruction> insts) {
    	this.insts = insts;
    }
    
    public Function getFunction() {
    	return f;
    }
    
    public Label getLabel() {
        return label;
    }

    public String getName() {
        return label.name;
    }

    public List<Instruction> getInsts() {
        return insts;
    }

	public List<BasicBlock> getSuccessors() {
		return successors;
	}
	
	public void addSuccessor(BasicBlock bb) {
		successors.add(bb);
	}

	public List<BasicBlock> getPredecessors() {
		return predecessors;
	}
	
	public void addPredecessor(BasicBlock bb) {
		predecessors.add(bb);
	}

	public List<BasicBlock> getBackEdges() {
		return backEdges;
	}
    
    public void addBackEdge(BasicBlock bb) {
    	backEdges.add(bb);
    }
}
