package test.compiler;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import uvm.objectmodel.SimpleObjectModel;
import uvm.type.Array;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Double;
import uvm.type.Ref;
import uvm.type.Struct;

public class RefOffset {
	SimpleObjectModel objModel = new SimpleObjectModel();

	@Test
	public void testNonRef() {
		Int i1 = Int.I1;
		int[] res_i1 = objModel.getBaseRefOffsets(i1);
		assertEquals("non ref Int<1> should have no ref offsets", 0, res_i1.length);
		
		Double d = Double.DOUBLE;
		int[] res_d = objModel.getBaseRefOffsets(d);
		assertEquals("non ref Double should have no ref offsets", 0, res_d.length);
	}

	@Test
	public void testRef() {
		Ref refVoid = Ref.REF_VOID;
		int[] res_refvoid = objModel.getBaseRefOffsets(refVoid);
		assertEquals("refvoid should have 1 ref offset", 1, res_refvoid.length);
		assertEquals("refvoid should have 1st ref offset as 0", 0, res_refvoid[0]);
		
		IRef irefVoid = IRef.IREF_VOID;
		int[] res_irefvoid = objModel.getBaseRefOffsets(irefVoid);
		assertEquals("irefvoid should have 1 ref offset", 1, res_irefvoid.length);
		assertEquals("irefvoid should have 1st ref offset as 0", 0, res_irefvoid[0]);
	}
	
	@Test
	public void testNonRefArray() {
		Array arrayInt64_5 = Array.findOrCreate(Int.I64, 5);
		int[] res_arrayInt64_5 = objModel.getBaseRefOffsets(arrayInt64_5);
		assertEquals("arrayInt64_5 should have 0 ref offsets", 0, res_arrayInt64_5.length);
	}
	
	@Test
	public void testRefArray() {
		Array arrayRefVoid_5 = Array.findOrCreate(Ref.REF_VOID, 5);
		int[] res_arrayRefVoid_5 = objModel.getBaseRefOffsets(arrayRefVoid_5);
		assertEquals("arrayRefVoid_5 should have 5 ref offsets", 5, res_arrayRefVoid_5.length);
		assertEquals("arrayRefVoid_5.refoffset[0] should be 0", 0, res_arrayRefVoid_5[0]);
		assertEquals("arrayRefVoid_5.refoffset[1] should be 8", 8, res_arrayRefVoid_5[1]);
		assertEquals("arrayRefVoid_5.refoffset[2] should be 16", 16, res_arrayRefVoid_5[2]);
		assertEquals("arrayRefVoid_5.refoffset[3] should be 24", 24, res_arrayRefVoid_5[3]);
		assertEquals("arrayRefVoid_5.refoffset[4] should be 32", 32, res_arrayRefVoid_5[4]);
	}
	
	@Test
	public void testStruct() {
		Struct struct1 = Struct.findOrCreateStruct(Arrays.asList(Ref.REF_VOID, Int.I64, Ref.REF_VOID, Int.I64));
		int[] res_struct1 = objModel.getBaseRefOffsets(struct1);
		assertEquals("struct1 should have 2 ref offsets", 2, res_struct1.length);
		assertEquals("struct1.refoffset[0] should be 0", 0, res_struct1[0]);
		assertEquals("struct1.refoffset[1] should be 16", 16, res_struct1[1]);
	}
	
	@Test
	public void testArrayOfStruct() {
		Struct struct1 = Struct.findOrCreateStruct(Arrays.asList(Ref.REF_VOID, Int.I64, Ref.REF_VOID, Int.I64));
		Array arrayOfStruct = Array.findOrCreate(struct1, 5);
		int[] res_arrayOfStruct = objModel.getBaseRefOffsets(arrayOfStruct);
		assertEquals("arrayOfStruct should have 10 ref offsets", 10, res_arrayOfStruct.length);
		assertEquals("arrayOfStruct.refoffset[0] should be 0", 0, res_arrayOfStruct[0]);
		assertEquals("arrayOfStruct.refoffset[1] should be 16", 16, res_arrayOfStruct[1]);
		assertEquals("arrayOfStruct.refoffset[2] should be 32", 32, res_arrayOfStruct[2]);
		assertEquals("arrayOfStruct.refoffset[3] should be 48", 48, res_arrayOfStruct[3]);
		assertEquals("arrayOfStruct.refoffset[4] should be 64", 64, res_arrayOfStruct[4]);
		assertEquals("arrayOfStruct.refoffset[5] should be 80", 80, res_arrayOfStruct[5]);
		
		Array arrayRef_5 = Array.findOrCreate(Ref.REF_VOID, 5);
		Struct struct2 = Struct.findOrCreateStruct(Arrays.asList(struct1, arrayRef_5));
		Array arrayOfStruct2 = Array.findOrCreate(struct2, 3);
		int[] res_arrayOfStruct2 = objModel.getBaseRefOffsets(arrayOfStruct2);
		assertEquals("arrayOfStruct2 should have 21 ref offsets", 21, res_arrayOfStruct2.length);
		assertEquals("arrayOfStruct2.refoffset[0] should be 0", 0, res_arrayOfStruct2[0]);
		assertEquals("arrayOfStruct2.refoffset[1] should be 16", 16, res_arrayOfStruct2[1]);
		assertEquals("arrayOfStruct2.resoffset[2] should be 32", 32, res_arrayOfStruct2[2]);
		assertEquals("arrayOfStruct2.refoffset[3] should be 40", 40, res_arrayOfStruct2[3]);
		assertEquals("arrayOfStruct2.refoffset[4] should be 48", 48, res_arrayOfStruct2[4]);
		assertEquals("arrayOfStruct2.refoffset[5] should be 56", 56, res_arrayOfStruct2[5]);
		assertEquals("arrayOfStruct2.refoffset[6] should be 64", 64, res_arrayOfStruct2[6]);

		assertEquals("arrayOfStruct2.refoffset[7] should be 72", 72, res_arrayOfStruct2[7]);
		assertEquals("arrayOfStruct2.refoffset[8] should be 88", 88, res_arrayOfStruct2[8]);
		assertEquals("arrayOfStruct2.refoffset[9] should be 104", 104, res_arrayOfStruct2[9]);
	}
}
