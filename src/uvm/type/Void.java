package uvm.type;

import uvm.Type;

public class Void extends Type {
	public static final Void T = new Void();

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String prettyPrint() {
        return "void";
    }

    @Override
    public int fitsInGPR() {
        return 0;
    }

    @Override
    public int fitsInFPR() {
        return 0;
    }

	@Override
	public boolean isReference() {
		return true;
	}
}
