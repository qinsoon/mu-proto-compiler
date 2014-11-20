package uvm.objectmodel;

import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;
import compiler.util.Alignment;
import uvm.MicroVM;
import uvm.Type;
import uvm.type.Ref;
import uvm.type.Struct;

public class SimpleObjectModel {
	public int getIRefOffsetFromRef(Ref ref) {
		return (int) Alignment.alignUp(getHeaderSize(ref.getReferenced()), ref.getReferenced().alignmentInBytes() << 3);
	}
	
	public int getOffsetFromStructIRef(Struct structT, int index) {
		return structT.getOffset(index);
	}
	
	/*
	 * >>>Type info<<< use simplest object type info for GC
	 * 
	 * 1. a length (4bytes) to describe bitmap length
	 * 2. a bitmap to describe reference(0 = this 8bytes contains no ref, 1 = this 8bytes is a ref)
	 * 
	 * E.g. int<32>
	 * GCHeader
	 * Length = 1
	 * Bitmap = 0
	 * 
	 * E.g. ref<double>
	 * GCHeader
	 * Length = 1
	 * Bitmap = 1
	 * 
	 * E.g. struct{int<32>, ref<int<32>>, int<64>, ref<int<64>>}
	 * GCHeader
	 * Length = 4      
	 * Bitmap = 0101
	 * 
	 */
	
	public static final int BITMAP_LENGTH_SIZE = 4;
	public static final int GC_HEADER_SIZE = 4;
	
	/*
	 * >>>Object Header<<<
	 * 
	 * For simplicity, just put type info in object header
	 */
	
	/**
	 * how many bytes is needed for object header for Type t
	 * @param t
	 * @return
	 */
	public int getHeaderSize(Type t) {
		int size = t.sizeInBytes();
		
		// how many bytes for bitmap
		// we use one bit to represent 8 bytes(2^3) ref/non-ref
		int bitmapSize = (int) Alignment.alignUp(size << 3, 8) >> 3;
		
		return  BITMAP_LENGTH_SIZE + GC_HEADER_SIZE + bitmapSize;
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