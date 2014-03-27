package uvm.inst;

import java.util.HashMap;
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
    
    HashMap<Label, Value> values = new HashMap<Label, Value>();
    
    public InstPhi(Type type, HashMap<Label, Value> values) {
        this.type = type;
        this.values = values;
        
        for (Value v : values.values())
            operands.add(v);
        
        opcode = OpCode.PHI;
    }
    
    public Map<Label, Value> getValues() {
        return values;
    }

    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        ret.append("(PHI ");
        for (Entry<Label, Value> i : values.entrySet()) {
            ret.append("[");
            ret.append(i.getKey().prettyPrint());
            ret.append(",");
            ret.append(i.getValue().prettyPrint());
            ret.append("]");
        }
        ret.append(")");
        return ret.toString();
    }
}
