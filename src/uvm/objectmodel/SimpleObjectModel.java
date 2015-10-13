package uvm.objectmodel;


import java.util.ArrayList;
import java.util.List;

import compiler.UVMCompiler;
import compiler.util.Alignment;
import uvm.MicroVM;
import uvm.Type;
import uvm.type.Array;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;

public class SimpleObjectModel {
	public int getIRefOffsetFromRef(Ref ref) {
		return (int) Alignment.alignUp(getHeaderSize(ref.getReferenced()), ref.getReferenced().alignmentInBytes() << 3);
	}
	
	public int getOffsetFromStructIRef(Struct structT, int index) {
		return structT.getOffset(index);
	}
	
	public int getOffsetFromArrayIRef(Array arrayT, int index) {
		return arrayT.getEleType().sizeInBytes() * index;
	}
	
	/*
	 * >>>Type info<<< use simplest object type info for GC
	 * 
	 * Use 8 bytes
	 * 
	 * 1st bit is never used (sign bit), left as zero
	 * otherwise the header initialization const may be larger than a java int
	 *
	 * for array and scalar
	 * 
	 * |   GC bits   |   type ID   |
	 * |  (8 bytes)  |  (8 bytes)  |
	 * 
	 */
	
	public static final long TYPEID_MASK = 0x00000000FFFFFFFFL;
	public static final long GCBITS_MASK = 0x7FFFFFFF00000000L;
	
	public static final int GCBITS_IN_BYTES = 4;
	public static final int TYPEID_SIZE_IN_BYTES = 4;
	
	public static final int HEADER_SIZE_IN_BYTES = GCBITS_IN_BYTES + TYPEID_SIZE_IN_BYTES;
	
	/*
	 * >>>Object Header<<<
	 * 
	 * For simplicity, just put type info in object header
	 */
	
	public long getHeaderInitialization(Type t) {
		int id = t.getID();
		long gcBits = getGCBitsInitialization(t);
		
		if (gcBits == 0)
			return id;
		else return ((long)((gcBits << (TYPEID_SIZE_IN_BYTES * 8)) & GCBITS_MASK)) + id;
	}
	
	private long getGCBitsInitialization(Type t) {
		return 0xBEEF;
	}

	/**
	 * bits for first fields at least significant bits
	 * @param t
	 * @return
	 */
	@Deprecated
	public long getReferenceBitMap(Type t) {		
		if (t instanceof Struct) {
			long ret = 0;
			
			Struct struct = (Struct) t;
			
			int expectOffset = 0;
			
			for (int i = 0; i < struct.getTypes().size(); i++) {
				Type ty = struct.getType(i);
				int offset = struct.getOffset(i);
				System.out.println("offset=" + offset + ",expect offset=" + expectOffset);
				
				if (offset < expectOffset) {
					continue;
				} else if (offset == expectOffset) {
					int bit = expectOffset/8;
					if (ty.isReference()) {
						// set the (expectOffset/8) bit to 1
						ret |= (1 << bit);
					} else {
						// set the bit to 0
						// do nothing
					}
				} else if (offset > expectOffset) {
					// do nothing
				}
				
				expectOffset += MicroVM.POINTER_SIZE / 8;
			}
			
			return ret;
		} else if (t.isReference())
			return 1;
		else return 0;
	}
	
	/**
	 * how many bytes is needed for object header for Type t
	 * @param t
	 * @return
	 */
	public int getHeaderSize(Type t) {
//		int size = t.sizeInBytes();
//		
//		// how many bytes for bitmap
//		// we use one bit to represent 8 bytes(2^3) ref/non-ref
//		int bitmapSizeInBits = size % 8 == 0? size / 8 : size /8 + 1;
//		int bitmapSizeInBytes = bitmapSizeInBits % 8 == 0 ? bitmapSizeInBits / 8 : bitmapSizeInBits / 8 + 1;
		
		return HEADER_SIZE_IN_BYTES;
	}
	
	public void layoutStruct(Struct struct) {
		final boolean DEBUG = true;
		
		if (DEBUG)
			System.out.println("layout struct " + struct.prettyPrint());
		
		List<Integer> offsets    = new ArrayList<Integer>();
		int cur = 0;
		
		for (int i = 0; i < struct.getTypes().size(); i++) {
			Type t = struct.getType(i);
			
			if (DEBUG)
				System.out.print("examining " + t.prettyPrint() + "...");
			
			if (cur % t.alignmentInBytes() != 0) {
				// mov cur to next aligned offset
				cur = (cur / t.alignmentInBytes() + 1) * t.alignmentInBytes();
				if (DEBUG)
					System.out.print("aligned to ");
			}
			offsets.add(cur);
			if (DEBUG)
				System.out.println("offset at " + cur);
			
			cur += t.sizeInBytes();
			if (DEBUG)
				System.out.println("  size is " + t.sizeInBytes());
		}
		
		if (DEBUG)
			System.out.println("struct size is " + cur);
		
		// if we need padding at the end
		if (cur % struct.alignmentInBytes() != 0) {
			cur  = (cur / struct.alignmentInBytes() + 1) * struct.alignmentInBytes();
			if (DEBUG)
				System.out.println("needs padding, after padding " + cur);
		}
		
		struct.setOffsets(offsets);
		struct.setSize(cur * 8);	// from bytes to bits
	}
	
	public int[] getBaseRefOffsets(Type t) {
		if (t instanceof Struct) {
			Struct structT = (Struct) t;
			ArrayList<Integer> temp = new ArrayList<Integer>();
			
			for (int i = 0; i < structT.getTypes().size(); i++) {
				int[] fieldRefOffsets = getBaseRefOffsets(structT.getType(i));
				int base = structT.getOffset(i);
				for (int off : fieldRefOffsets)
					temp.add(base + off);
			}
			
			int[] ret = new int[temp.size()];
			for (int i = 0; i < ret.length; i++)
				ret[i] = temp.get(i);
			return ret;
		} else if (t instanceof Array) {
			Array arrayT = (Array) t;
			Type eleT    = arrayT.getEleType();
			int[] eleTRefOffsets = getBaseRefOffsets(eleT);
			
			if (eleTRefOffsets.length == 0) {
				return new int[0];
			} else {
				int[] ret = new int[eleTRefOffsets.length * arrayT.getLength()];
				for (int i = 0; i < arrayT.getLength(); i++) {
					for (int j = 0; j < eleTRefOffsets.length; j++)
					ret[i * eleTRefOffsets.length + j] = i * eleT.sizeInBytes() + eleTRefOffsets[j];
				}
				return ret;
			}			
		} else{
			// t is scalar
			if (t.isBaseRef()) {
				return new int[] {0};
			} else {
				return new int[0];
			}
		}
			
	}
	
	public int[] getIRefOffsets(Type t) {
		if (t instanceof Struct) {
			Struct structT = (Struct) t;
			ArrayList<Integer> temp = new ArrayList<Integer>();
			
			for (int i = 0; i < structT.getTypes().size(); i++) {
				int[] fieldRefOffsets = getIRefOffsets(structT.getType(i));
				int base = structT.getOffset(i);
				for (int off : fieldRefOffsets)
					temp.add(base + off);
			}
			
			int[] ret = new int[temp.size()];
			for (int i = 0; i < ret.length; i++)
				ret[i] = temp.get(i);
			return ret;
		} else if (t instanceof Array) {
			Array arrayT = (Array) t;
			Type eleT    = arrayT.getEleType();
			int[] eleTRefOffsets = getIRefOffsets(eleT);
			
			if (eleTRefOffsets.length == 0) {
				return new int[0];
			} else {
				int[] ret = new int[eleTRefOffsets.length * arrayT.getLength()];
				for (int i = 0; i < arrayT.getLength(); i++) {
					for (int j = 0; j < eleTRefOffsets.length; j++)
					ret[i * eleTRefOffsets.length + j] = i * eleT.sizeInBytes() + eleTRefOffsets[j];
				}
				return ret;
			}			
		} else{
			// t is scalar
			if (t.isIRef()) {
				return new int[] {0};
			} else {
				return new int[0];
			}
		}
			
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
		}
		else if (t instanceof uvm.type.Stack) 
			return MicroVM.POINTER_SIZE / 8;
		else {
			UVMCompiler.error("Unknown type when trying to get alignment: " + t.prettyPrint());
			return -1;
		}
	}
}
