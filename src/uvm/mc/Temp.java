package uvm.mc;

import java.util.HashMap;

public class Temp extends Operand{
    public static final int RES_REG     = 0;
    public static final int RET_REG     = 1;
    public static final int PARAM_REG   = 2;
    public static final int MACHINE_REG = 3;
    public static final int OTHER_SYMBOL_REG  = 4;
    
    int type;
    String name;
    
    private Temp(String name, int type) {
        this.name = name;
        this.type = type;
    }
    
    static final HashMap<String, Temp> temps = new HashMap<String, Temp>();
    
    public static Temp findOrCreate(String name, int type) {
        if (temps.containsKey(name))
            return temps.get(name);
        
        Temp ret = new Temp(name, type);
        temps.put(name, ret);
        return ret;
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
    
    public static void clearTemps() {
        temps.clear();
    }
    
    @Override
    public String prettyPrint() {
        return "%" + name;
    }
}
