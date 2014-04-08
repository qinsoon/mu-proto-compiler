package uvm.mc;

public abstract class AbstractMCDriver {
    public abstract AbstractMachineCode genMove(MCRegister dest, MCOperand src);
}
