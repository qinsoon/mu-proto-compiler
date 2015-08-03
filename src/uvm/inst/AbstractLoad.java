package uvm.inst;

import uvm.Instruction;
import uvm.Value;
import uvm.type.IRef;

public abstract class AbstractLoad extends Instruction {
	protected Value addr;
	protected IRef type;
	
	public AbstractLoad(IRef type, Value addr) {
		this.type = type;
		this.addr = addr;
		
		operands.add(addr);
	}
}
