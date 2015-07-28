package uvm;

import uvm.mc.MCDPImmediate;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCSPImmediate;

public class IntImmediate extends ImmediateValue {
    long value;
    Type type;
    
    public IntImmediate(Type type, long value) {
        this.value = value;
        this.type = type;
        this.opcode = OpCode.INT_IMM;
    }
    
    @Override
    public String prettyPrint() {
        return "(INT_IMM " + value + ")";
    }
    
    public long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o == null)
    		return false;
    	
    	if (o instanceof IntImmediate && ((IntImmediate) o).value == value && ((IntImmediate) o).type.equals(type))
    		return true;    	
    	else return false;
    }
}
