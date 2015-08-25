package uvm.inst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.Label;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstPhi extends Instruction {
    Type type;
    
    public InstPhi(Type type, List<Value> values) {
        this.type = type;
        
        for (Value v : values)
            operands.add(v);
        
        opcode = OpCode.PHI;
    }
    
    public Value getValue(Label a) {
    	for (int i = 0; i < operands.size(); i += 2) {
    		Value label = operands.get(i+1);
    		if (label.equals(a))
    			return operands.get(i);
    	}
    	
    	return null;
    }
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("(PHI ");
        for (int i = 0; i < operands.size(); i += 2) {
        	Value label = operands.get(i+1);
        	Value value = operands.get(i);
        	ret.append("[");
        	ret.append(label.prettyPrint());
        	ret.append(":");
        	ret.append(value.prettyPrint());
        	ret.append("]");
        }
        ret.append(")");
        return ret.toString();
    }
}
