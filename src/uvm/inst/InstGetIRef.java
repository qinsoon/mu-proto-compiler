package uvm.inst;

import uvm.Instruction;
import uvm.OpCode;
import uvm.Type;
import uvm.Value;

public class InstGetIRef extends Instruction {
	Type referent;
	Value ref;
	
	public InstGetIRef(Type referent, Value ref) {
		this.referent = referent;
		this.ref = ref;
		
		this.opcode = OpCode.GETIREF;
	}

	@Override
	public String prettyPrint() {
		return "(GETIREF " + referent.prettyPrint() + " " + ref.prettyPrint() + ")";
	}

	public Type getReferentType() {
		return referent;
	}

	public Value getRef() {
		return ref;
	}

}
