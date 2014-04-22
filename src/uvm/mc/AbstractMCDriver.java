package uvm.mc;

public abstract class AbstractMCDriver {
    public abstract AbstractMachineCode genMove(MCRegister dest, MCOperand src);
    public abstract int getNumberOfGPR();
    public abstract String getGPRName(int i);
    public abstract int getNumberOfGPRParam();
    public abstract String getGPRParamName(int i);
    public abstract int getNumberOfGPRRet();
    public abstract String getGPRRetName(int i);
}
