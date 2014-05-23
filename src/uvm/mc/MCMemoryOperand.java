package uvm.mc;

/*
 * reference to JikesRVM's org.jikesrvm.compiler.opt.ir.operand.MemoryOperand
 */
public class MCMemoryOperand extends MCOperand {

    // base register
    MCRegister base = null;
    // index register
    MCRegister index = null;
    // scale value (log power of 2). valid values are 0,1,2,3
    byte scale = 0;
    // displacement
    int disp = 0;
    MCLabel dispLabel = null; // displacement represented by a label
    
    // number of bytes being accessed (1,2,4,8)
    byte size;
    
    // [base]+[index]*(2^scale)+disp/dispLabel
    @Override
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (base != null)
            ret.append("[" + base.prettyPrint() + "]+");
        if (index != null)
            ret.append("[" + index.prettyPrint() + "]*");
        ret.append("(2^" + scale + ")+" + disp);
        if (dispLabel != null)
            ret.append("+" + dispLabel.prettyPrint());
        return ret.toString();
    }

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

    public int getDisp() {
        return disp;
    }

    public void setDisp(int disp) {
        this.disp = disp;
    }

    public MCLabel getDispLabel() {
        return dispLabel;
    }

    public void setDispLabel(MCLabel dispLabel) {
        this.dispLabel = dispLabel;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(byte size) {
        this.size = size;
    }

}
