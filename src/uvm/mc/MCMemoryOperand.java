package uvm.mc;

/*
 * reference to JikesRVM's org.jikesrvm.compiler.opt.ir.operand.MemoryOperand
 */
public class MCMemoryOperand extends MCOperand {

    // base register
    MCRegister base;
    // index register
    MCRegister index;
    // scale value (log power of 2). valid values are 0,1,2,3
    byte scale;
    // displacement
    int disp;
    MCLabel dispLabel; // displacement represented by a label
    
    // number of bytes being accessed (1,2,4,8)
    byte size;
    
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (base != null)
            ret.append("[" + base.prettyPrint() + "]+");
        if (index != null)
            ret.append("[" + base.prettyPrint() + "]*");
        ret.append("(2^" + scale + ")+" + disp);
        if (dispLabel != null)
            ret.append("+" + dispLabel.prettyPrint());
        return ret.toString();
    }

}
