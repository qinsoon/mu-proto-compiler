package uvm;

import java.util.ArrayList;
import java.util.List;

/**
 * symbolic register
 *
 */
public class Register extends Value{
    String name;
    
    Instruction def;    
    List<Instruction> uses = new ArrayList<Instruction>();
    
    Register(String name) {
        this.name = name;
        this.opcode = OpCode.REG;
    }

    @Override
    public String prettyPrint() {
        return "%" + name;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public boolean isRegister() {
        return true;
    }    
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Register))
            return false;
        
        return name.equals(((Register)o).name);
    }

    public Instruction getDef() {
        return def;
    }

    public void setDef(Instruction def) {
        this.def = def;
    }
    
    public void addUse(Instruction e) {
        uses.add(e);
    }
    
    public List<Instruction> getUses() {
        return uses;
    }
    
    public boolean usesOnlyOnce() {
        return uses.size() == 1;
    }
}
