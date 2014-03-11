package uvm;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import uvm.metadata.Const;

public class Function {
    private static int funcCount = 0;
    
    int ID;
    
    String name;
    FunctionSignature sig;
    
    boolean defined = false;
    
    // to define a function. The following need to be provided
    
    HashMap<String, Const> constPool;
    List<BasicBlock> BBs;
    
    // IR tree
    public List<IRTreeNode> tree = new ArrayList<IRTreeNode>();
    
    public HashMap<String, Register> registers = new HashMap<String, Register>();
    public Register findOrCreateRegister(String name) {
        Register ret = registers.get(name);
        if (ret == null) {
            ret = new Register(name);
            registers.put(name, ret);
        }
        
        return ret;
    }
    
    public Function(String name, FunctionSignature sig) {
        this.ID = funcCount;
        funcCount++;
        
        this.name = name;
        this.sig = sig;
    }
    
    public void defineFunction(HashMap<String, Const> constPool, List<BasicBlock> BBs) {
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
//    
//    public HashMap<String, IRTreeNode> getIdMap() {
//        return idMap;
//    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public FunctionSignature getSig() {
        return sig;
    }

    public HashMap<String, Const> getConstPool() {
        return constPool;
    }

    public List<BasicBlock> getBBs() {
        return BBs;
    }
}
