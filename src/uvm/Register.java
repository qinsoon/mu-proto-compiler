package uvm;

/**
 * symbolic register
 *
 */
public class Register extends Value{
    String name;
    Instruction def;
    
    Register(String name) {
        this.name = name;
    }

    @Override
    public String prettyPrint() {
        return "%" + name;
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
}
