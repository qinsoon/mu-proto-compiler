package uvm;

import uvm.mc.MCDPImmediate;
import uvm.mc.MCIntImmediate;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCSPImmediate;

public abstract class Value extends IRTreeNode {
    public abstract boolean isRegister();
    
    @Override
    public MCOperand getMCOp() {
    	if (mcOp != null)
    		return mcOp;
    	else {
    		if (opcode == OpCode.INT_IMM)
    			return new MCIntImmediate(((IntImmediate)this).value);
    		else if (opcode == OpCode.FP_DP_IMM)
    			return new MCDPImmediate(((FPImmediate)this).value);
    		else if (opcode == OpCode.FP_SP_IMM)
    			return new MCSPImmediate((float) ((FPImmediate)this).value);
    		else if (opcode == OpCode.LABEL)
    			return new MCLabel(((Label)this).name);
    		else{
    			System.out.println(this.getClass() + ": getMCOP when mcOp is null. Need to implement cases for label, reg");
    			System.exit(-1);
    	    	return null;	// not reached
    		}
    	}
    }
}
