package uvm;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;

/**
 * symbolic register
 *
 */
public class Register extends Value{
    String name;
    
    Instruction def;    
    List<Instruction> uses = new ArrayList<Instruction>();
    
    Register(String name, uvm.Type type) {
        this.name = name;
        this.opcode = getOpCodeFromType(type);
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
    
    public static final int getOpCodeFromType(uvm.Type type) {
        if (type instanceof uvm.type.Int) {
            uvm.type.Int intType = (uvm.type.Int) type;
            if (intType.size() == 1)
                return OpCode.REG_I1;
            else if (intType.size() == 8)
                return OpCode.REG_I8;
            else if (intType.size() == 16)
                return OpCode.REG_I16;
            else if (intType.size() == 32)
                return OpCode.REG_I32;
            else if (intType.size() == 64)
                return OpCode.REG_I64;
            else {
                UVMCompiler.error("unimplemented int register size");
            }
        } else if (type instanceof uvm.type.Double)
            return OpCode.REG_DP;
        else if (type instanceof uvm.type.Float)
            return OpCode.REG_SP;
        else {
            UVMCompiler.error("unexpected register type: " + type.prettyPrint());
        }
        return -1;
    }
}
