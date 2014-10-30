package uvm.objectmodel;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;
import uvm.MicroVM;
import uvm.Type;
import uvm.type.Struct;

public class SimpleObjectModel {
	public int getOffsetFromStructIRef(Struct structT, int index) {
		return structT.getOffset(index);
	}
	
	public void layoutStruct(Struct struct) {
		List<Integer> offsets = new ArrayList<Integer>();
		int cur = 0;
		
		for (int i = 0; i < struct.getTypes().size(); i++) {
			Type t = struct.getType(i);
			
			if (cur % t.alignmentInBytes() != 0) {
				// mov cur to next aligned offset
				cur = (cur / t.alignmentInBytes() + 1) * t.alignmentInBytes();
			}
			offsets.add(cur);
			
			cur += t.sizeInBytes();
		}
		
		// if we need padding at the end
		if (cur % struct.alignmentInBytes() != 0)
			cur  = (cur / struct.alignmentInBytes() + 1) * struct.alignmentInBytes();
		
		struct.setOffsets(offsets);
		struct.setSize(cur);
	}
	
	public int getAlignment(Type t) {
		if (t instanceof uvm.type.AbstractPointerType)
			return MicroVM.POINTER_SIZE / 8;
		else if (t instanceof uvm.type.Double)
			return 8;
		else if (t instanceof uvm.type.Float)
			return 4;
		else if (t instanceof uvm.type.Int) {
			int size = t.sizeInBytes();
			int align = 1;
			while (align < size) {
				align *= 2;
			}
			return align;
		}
		else if (t instanceof uvm.type.Void) 
			return 0;
		else if (t instanceof uvm.type.Array) 
			return ((uvm.type.Array) t).getEleType().alignmentInBytes();
		else if (t instanceof uvm.type.Struct) {
			int align = 0;
			for (Type eleType : ((uvm.type.Struct) t).getTypes()) {
				if (eleType.alignmentInBytes() > align)
					align = eleType.alignmentInBytes();
			}
			return align;
		} else {
			UVMCompiler.error("Unknown type when trying to get alignment: " + t.prettyPrint());
			return -1;
		}
	}
}
