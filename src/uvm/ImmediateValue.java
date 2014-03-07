package uvm;

public abstract class ImmediateValue extends Value {
    @Override
    public boolean isRegister() {
        return false;
    }
}
