package uvm;

import uvm.mc.MCRegister;

class FrameSlot {
	int offset;			// offset from this frame
	int slot;
	MCRegister value;
	IRTreeNode hllValue;
	
	public FrameSlot(int offset, int slot, MCRegister value, IRTreeNode hllValue) {
		super();
		this.offset = offset;
		this.slot = slot;
		this.value = value;
		this.hllValue = hllValue;
	}
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public MCRegister getValue() {
		return value;
	}
	public void setValue(MCRegister value) {
		this.value = value;
	}
	public IRTreeNode getHllValue() {
		return hllValue;
	}
	public void setHllValue(IRTreeNode hllValue) {
		this.hllValue = hllValue;
	}
}
