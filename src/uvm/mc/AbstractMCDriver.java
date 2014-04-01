package uvm.mc;

public abstract class AbstractMCDriver {
    public abstract AbstractMachineCode genMove(MCOperand dest, MCOperand src);
}
