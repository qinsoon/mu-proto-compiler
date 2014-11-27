package uvm.type;

import uvm.Type;

public class Array extends Type {
    Type eleType;
    int length;
    
    public Type getEleType() {
		return eleType;
	}

	public int getLength() {
		return length;
	}

	protected Array(Type eleType, int length) {
        super();
        this.eleType = eleType;
        this.length = length;
    }

    @Override
    public int size() {
        return eleType.size() * length;
    }

    @Override
    public String prettyPrint() {
        return "array<" + eleType.prettyPrint() + " " + length + ">";
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
		return false;
	}
}
