package uvm.mc;

import java.util.ArrayList;
import java.util.List;

/*
 * This class represent basic block as machine code level,  
 * as described in <Linear Scan Register Allocation in the 
 * Context of SSA Form and Register Constraints> Fig 6
 */
public class MCBasicBlock {
    MCLabel label;
    
    // machine basic block has at most two sucessors, and several predecessors
    List<MCBasicBlock> successors   = new ArrayList<MCBasicBlock>();
    List<MCBasicBlock> predecessors = new ArrayList<MCBasicBlock>();
    
    AbstractMachineCode phi;            // first phi instruction
    List<AbstractMachineCode> mc = new ArrayList<AbstractMachineCode>();    // first/last instruction can be found here
    
    public List<MCRegister> liveIn = new ArrayList<MCRegister>();
    
    public MCBasicBlock(String name) {
        this.label = new MCLabel(name);
    }
    
    public MCBasicBlock(MCLabel label) {
        this.label = label;
    }
    
    public String getName() {
        return label.getName();
    }
    
    public MCLabel getLabel() {
        return label;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof MCBasicBlock && getName().equals(((MCBasicBlock)o).getName()))
            return true;
        
        return false;
    }
    
    public String prettyPrintWithPreAndSucc() {
        StringBuilder ret = new StringBuilder();
        ret.append("BB: #" + getName() + "\n");
        ret.append("  predecessors: {");
        for (MCBasicBlock p : getPredecessors())
            ret.append(p.getName() + " ");
        ret.append("}\n");
        
        ret.append("  sucessors: {");
        for (MCBasicBlock s : getSuccessor()) 
            ret.append(s.getName() + " ");
        ret.append("}\n");
        
        ret.append(prettyPrint());
        return ret.toString();
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("#" + label.name + ": {\n");
        for (AbstractMachineCode c : mc) {
            ret.append("  " + c.prettyPrintNoLabel() + "\n");
        }
        ret.append("}");
        return ret.toString();
    }
    
    public List<MCBasicBlock> getSuccessor() {
        return successors;
    }
    public void addSuccessor(MCBasicBlock successor) {
        this.successors.add(successor);
    }
    public List<MCBasicBlock> getPredecessors() {
        return predecessors;
    }
    public void addPredecessors(MCBasicBlock predecessor) {
        this.predecessors.add(predecessor);
    }
    public AbstractMachineCode getPhi() {
        return phi;
    }
    public void setPhi(AbstractMachineCode phi) {
        this.phi = phi;
    }
    public AbstractMachineCode getFirst() {
        if (mc.isEmpty())
            return null;
        
        return mc.get(0);
    }
    public AbstractMachineCode getLast() {
        if (mc.isEmpty())
            return null;
        
        return mc.get(mc.size() - 1);
    }
    public List<AbstractMachineCode> getMC() {
        return mc;
    }
    public void setMC(List<AbstractMachineCode> mc) {
        this.mc = mc;
    }
    public void addMC(AbstractMachineCode c) {
        this.mc.add(c);
    }
}
