package uvm;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import compiler.UVMCompiler;
import uvm.mc.AbstractMachineCode;
import uvm.metadata.Const;

public class Function {
    private static int funcCount = 0;
    
    int ID;
    
    String name;
    FunctionSignature sig;
    
    uvm.Label funcLabel;
    
    boolean defined = false;
    
    // to define a function. The following need to be provided
    
    HashMap<String, ImmediateValue> constPool;
    List<BasicBlock> BBs;
    
    BasicBlock CFG;
    
    // IR tree
    public List<IRTreeNode> tree = new ArrayList<IRTreeNode>();
    
    public HashMap<String, Register> registers = new HashMap<String, Register>();
    
    public Register findOrCreateRegister(String name, uvm.Type type) {
        Register ret = registers.get(name);
        if (ret == null) {
            ret = new Register(name, type);
            registers.put(name, ret);
        }
        
        return ret;
    }
    
    public HashMap<String, Label> labels = new HashMap<String, Label>();
    
    public Label findOrCreateLabel(String name) {
        if (labels.containsKey(name))
            return labels.get(name);
        
        Label ret = new Label(name);
        labels.put(name, ret);
        return ret;
    }    

    public void resolveLabels() {
        for (BasicBlock bb : BBs) {
            Label label = labels.get(bb.getName());
            if (label == null)
                UVMCompiler.error("Cant find label for basic block " + bb.getName());
            
            bb.getInsts().get(0).setLabel(label);
        } 
    }
    
    public Function(String name, FunctionSignature sig) {
        this.ID = funcCount;
        funcCount++;
        
        this.name = name;
        this.sig = sig;
        
        this.funcLabel = findOrCreateLabel(name);
    }
    
    public void defineFunction(HashMap<String, ImmediateValue> constPool, List<BasicBlock> BBs) {
        if (defined)
            throw new RuntimeException("Redefining function: " + name + ", probably by an error");
        
        this.constPool = constPool;
        this.BBs = BBs;
        defined = true;
    }
    
    @Override
    public String toString() {
        return name + " = " + sig;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
    
    public boolean isMain() {
    	return name.equals("main");
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public HashMap<String, ImmediateValue> getConstPool() {
        return constPool;
    }
    
    public BasicBlock getBB(String name) {
    	for (BasicBlock bb : BBs) {
    		if (bb.getName().equals(name))
    			return bb;
    	}
    	
    	return null;
    }

    public List<BasicBlock> getBBs() {
        return BBs;
    }

    public uvm.Label getFuncLabel() {
        return funcLabel;
    }
    
    public void setCFGEntry(BasicBlock bb) {
    	this.CFG = bb;
    }
    
    public BasicBlock getCFG() {
    	return CFG;
    }
}
