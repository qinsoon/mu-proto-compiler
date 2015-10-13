package uvm.inst;

public class InstGC extends AbstractInternalInstruction {

	@Override
	public String prettyPrint() {
		return "(GC)";
	}

	@Override
    public boolean needsToExpandIntoRuntimeCall() {
    	return true;
    }
}
