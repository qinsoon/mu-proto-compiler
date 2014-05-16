package uvm.mc;

public abstract class AbstractMCDriver {
    public abstract AbstractMachineCode genMove(MCRegister dest, MCOperand src);
    public abstract AbstractMachineCode genDPMove(MCRegister dest, MCOperand src);
    public abstract AbstractMachineCode genSPMove(MCRegister dest, MCOperand src);
    
    public abstract AbstractMachineCode genJmp(MCLabel target);
    
    public abstract AbstractMachineCode genRet();
    
    public abstract AbstractMachineCode genNop();
    
    public abstract AbstractMachineCode genOppositeCondJump(AbstractMachineCode orig);
    
    public abstract int getNumberOfGPR();
    public abstract String getGPRName(int i);
    public abstract int getNumberOfGPRParam();
    public abstract String getGPRParamName(int i);
    public abstract int getNumberOfGPRRet();
    public abstract String getGPRRetName(int i);
}
