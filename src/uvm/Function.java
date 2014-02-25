package uvm;

import java.util.HashMap;
import java.util.List;

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
}
