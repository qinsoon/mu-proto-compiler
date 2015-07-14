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
	 * 
	 * 1. If the 2nd bit is 0 (Scalar)
	 * 
	 * (1) if the 3rd bit is 1
	 * 
	 * high                                                                     low
	 * | 001xxxxx xxxxxxxx | xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx|
	 *    GC bits            Reference Bitmap
	 *  
	 * (2) if the 3rd bit is 0
	 * 
	 * high																		 low
	 * | 000xxxxx xxxxxxxx | xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx|
	 *    GC bits            Pointer to external reference bitmap
	 * 
	 * 
	 * E.g. int<32>
	 * Bitmap = 0
	 * 
	 * E.g. ref<double>
	 * Bitmap = 1
	 * 
	 * E.g. struct{int<32>, int<32>, ref<int<32>>, int<64>, ref<int<64>>}  
	 * Bitmap = 0101
	 * 
	 * 2. If the 2nd bit is 1 (Array)
	 * 
	 * high
	 * | 01yxxxxx xxxxxxxx | xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx |
	 *    GC bits			 Length of array
	 *    
	 * the 3rd bit tells if the array element is reference or not 
	 * 
	 */
	
	public static final long BITMAP_MASK = 0x00FFFFFFL;
	public static final long GCBITS_MASK = 0x7F000000L;
	
//	public static final int BITMAP_LENGTH_SIZE_IN_BYTES = 4;
	public static final int GC_HEADER_SIZE_IN_BYTES = 2;
	public static final int BITMAP_SIZE_IN_BYTES = 6;
	
	/*
	 * >>>Object Header<<<
	 * 
	 * For simplicity, just put type info in object header
	 */
	
	public long getHeaderInitialization(Type t) {
		if (t instanceof Array) {
			long ret;
			if (((Array) t).getEleType().isReference()) {
				// 0110 0000 ...
				ret = 0x60000000L;
			} else
				// 0100 0000 ...
				ret = 0x40000000L;
			
			// put length into last 6 bytes
			int length = ((Array) t).getLength();
			int maskedLength = length & 0x00FFFFFF;
			if (length != maskedLength) {
				UVMCompiler.error("Length excceds max length that can be put in the header. ");
			}
			
			ret |= maskedLength;
			return ret;
		}
		
		// we use 1 bit in the bitmap to represent 8bytes
		// check if we can fit the type into a 48bits bitmap
		if (t.sizeInBytes() > 48 * 8) {
			// we use 48bits as pointer to an external bitmap
			UVMCompiler.error("unimplemented for external bitmap");
			return 0;
		} else {
			long ret = (1 << 62);
			long gc = getGCBitsInitialization(t);
			long bitmap = getReferenceBitMap(t);
			ret |= gc;
			ret |= bitmap;
			return ret;
		}

	}
	
	private long getGCBitsInitialization(Type t) {
		return 0;
	}

	/**
	 * bits for first fields at least significant bits
	 * @param t
	 * @return
	 */
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
		
		return  GC_HEADER_SIZE_IN_BYTES + BITMAP_SIZE_IN_BYTES;
	}
	
	public void layoutStruct(Struct struct) {
		final boolean DEBUG = true;
		
		if (DEBUG)
			System.out.println("layout struct " + struct.prettyPrint());
		
		List<Integer> offsets = new ArrayList<Integer>();
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
