package uvm.mc;

import java.util.HashMap;

import uvm.OpCode;

public class MCRegister extends MCOperand{
    public static final int RES_REG     = 0;
    public static final int RET_REG     = 1;
    public static final int PARAM_REG   = 2;
    public static final int MACHINE_REG = 3;
    public static final int OTHER_SYMBOL_REG  = 4;
    
    public static final int DATA_DP = 100;
    public static final int DATA_SP = 101;
    public static final int DATA_GPR = 102;
    public static final int DATA_MEM = 103;
    
    int type;
    int dataType;
    String name;
    
    public int weight = 0;  // used by linear scan to decide which register to spill
    
    MCRegister join;

    public MCRegister(String name, int type, int dataType) {
        this.name = name;
        this.type = type;        
        this.dataType = dataType;
        
        this.join = this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof MCRegister && ((MCRegister) o).type == this.type && ((MCRegister) o).name.equals(this.name))
            return true;
        
        return false;
    }
    
    public void setREP(MCRegister reg) {
        this.join = reg;
    }
    
    public MCRegister REP() {
        if (this.join == this)
            return this;
        else return join.REP();
    }
    
    public static HashMap<String, MCRegister> temps = new HashMap<String, MCRegister>();
    
    /**
     * do not use this method. Instead use CompiledFunction.findOrCreateRegister
     * this method is only supposed to use during emitting code (by BURM)
     * @param name
     * @param type
     * @return
     */
    public static MCRegister findOrCreate(String name, int type, int dataType) {
        if (temps.containsKey(name))
            return temps.get(name);
        
        MCRegister ret = new MCRegister(name, type, dataType);
        temps.put(name, ret);
        return ret;
    }
    
    public static void clearTemps() {
        temps = new HashMap<String, MCRegister>();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public int getType() {
        return type;
    }
    
    public int getREPType() {
        if (join == this)
            return type;
        else return join.getREPType();
    }
    
    @Override
    public String prettyPrint() {
        if (join == this)
            return "%" + name;
        else return "%" + name + "(REP=" + join.prettyPrint() + ")";
    }
    
    @Override
    public String prettyPrintREPOnly() {
        if (join == this)
            return "%" + name;
        else return join.prettyPrintREPOnly();
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
    
    public static int dataTypeFromOpCode(int opCode) {
    	switch (opCode) {
    	case OpCode.REG_DP: return DATA_DP;
    	case OpCode.REG_SP: return DATA_SP;
    	default:			return DATA_GPR;
    	}
    }
}
