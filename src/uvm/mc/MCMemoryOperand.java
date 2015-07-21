package uvm.mc;

/*
 * reference to JikesRVM's org.jikesrvm.compiler.opt.ir.operand.MemoryOperand
 */
public abstract class MCMemoryOperand extends MCOperand {

    // base register
    MCRegister base = null;

    // number of bytes being accessed (1,2,4,8)
    byte size;
    
    public MCMemoryOperand() {}
    
    public MCMemoryOperand(MCRegister base) {
    	this.base = base;
    }
    
    public MCRegister getBase() {
        return base;
    }

    public void setBase(MCRegister base) {
        this.base = base;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(byte size) {
        this.size = size;
    }

}
