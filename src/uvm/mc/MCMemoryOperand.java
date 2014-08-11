package uvm.mc;

/*
 * reference to JikesRVM's org.jikesrvm.compiler.opt.ir.operand.MemoryOperand
 */
public abstract class MCMemoryOperand extends MCOperand {

    // base register
    MCRegister base = null;
    // index register
    MCRegister index = null;
    // scale value (log power of 2). valid values are 0,1,2,3
    byte scale = 0;    
    // number of bytes being accessed (1,2,4,8)
    byte size;
    
    public MCRegister getBase() {
        return base;
    }

    public void setBase(MCRegister base) {
        this.base = base;
    }

    public MCRegister getIndex() {
        return index;
    }

    public void setIndex(MCRegister index) {
        this.index = index;
    }

    public byte getScale() {
        return scale;
    }

    public void setScale(byte scale) {
        this.scale = scale;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(byte size) {
        this.size = size;
    }

}
