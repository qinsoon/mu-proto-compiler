package uvm.inst;

import java.util.List;

import uvm.Function;
import uvm.Instruction;
import uvm.OpCode;

public class InstCall extends Instruction {
    Function callee;
    List<uvm.Value> arguments;
    
    public InstCall(Function callee, List<uvm.Value> arguments) {
        this.callee = callee;
        this.arguments = arguments;
        this.opcode = OpCode.CALL;
    }
    
    @Override
    public String prettyPrint() {
        // TODO Auto-generated method stub
        return "CALL";
    }

    public Function getCallee() {
        return callee;
    }

    public List<uvm.Value> getArguments() {
        return arguments;
    }

}
