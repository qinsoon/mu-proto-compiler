package uvm;

import java.util.ArrayList;
import java.util.List;

import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;

public class CompiledFunction {
    Function origin;
    
    // MC
    public List<AbstractMachineCode> mc = new ArrayList<AbstractMachineCode>();
    
    // MC BB
    public List<MCBasicBlock> BBs = new ArrayList<MCBasicBlock>();
    public MCBasicBlock entryBB;
    
    public CompiledFunction(Function origin) {
        this.origin = origin;
    }
    
    public Function getOriginFunction() {
        return origin;
    }
    
    public void addMachineCode(AbstractMachineCode code) {
        mc.add(code);
    }
    
    public void addMachineCode(List<AbstractMachineCode> code) {
        mc.addAll(code);
    }
    
    public List<AbstractMachineCode> getMachineCode() {
        return mc;
    }
}
