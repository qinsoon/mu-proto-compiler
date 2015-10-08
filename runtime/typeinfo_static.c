#include "typeinfo.h"
#include "debug.h"
#include "heap.h"
#include <stdlib.h>
#include <stdio.h>

int typeCount;
TypeInfo** typeInfoTable;

void fillTypeInfo(TypeInfo* t, int64_t id, int64_t size, int64_t align,
		int64_t eleSize, int64_t length,
		int64_t nFixedRefOffsets, int64_t nFixedIRefOffsets) {
	t->id 				= id;
	t->size 			= size;
	t->align 			= align;
	t->eleSize 			= eleSize;
	t->length 			= length;
	t->nFixedRefOffsets = nFixedRefOffsets;
	t->nFixedIRefOffsets= nFixedIRefOffsets;
}

TypeInfo* allocScalarTypeInfo(int64_t id, int64_t size, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets) {
	TypeInfo* ret = (TypeInfo*) malloc(sizeof(TypeInfo) + (nRefOffsets + nIRefOffsets) * sizeof(int64_t));
	fillTypeInfo(ret, id, size, align, size, 1, nRefOffsets, nIRefOffsets);
	return ret;
}

TypeInfo* allocArrayTypeInfo (int64_t id, int64_t eleSize, int64_t length, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets) {
	TypeInfo* ret = (TypeInfo*) malloc(sizeof(TypeInfo) + (nRefOffsets + nIRefOffsets) * sizeof(int64_t));
	fillTypeInfo(ret, id, eleSize * length, align, eleSize, length, nRefOffsets, nIRefOffsets);
	return ret;
}

//TypeInfo* allocHybridTypeInfo(int64_t id, int64_t size, int64_t align, int64_t eleSize, int64_t length, int64_t nFixedRefOffsets, int64_t nVarRefOffsets) {
//	uVM_fail("allocHybridTypeInfo() unimplemented");
//	return NULL;
//}

TypeInfo* getTypeInfo(Address ref) {
	int id = getTypeID(ref);
	uVM_assert(id >= 0 && id < typeCount, "invalid type id in getTypeInfo()");
	return typeInfoTable[id];
}

int getTypeID(Address ref) {
	uint64_t header = * ((uint64_t*)ref);
//	printf("header = %llx\n", header);
	int id = (int) (header & 0xFFFFFFFF);
//	printf("id = %d\n", id);
	return id;
}

void printObject(Address ref) {
	TypeInfo* tinfo = getTypeInfo(ref);

	if (tinfo != NULL) {
		int64_t size = tinfo->size;

		printf("HEADER \n");
		printf("Address 0x%llx\t| 0x%llx\n", ref, *((Address*)ref));
		printf("OBJ\n");
		Address cur = ref + OBJECT_HEADER_SIZE;
		for (; cur < ref + OBJECT_HEADER_SIZE + size; cur += WORD_SIZE) {
			printf("Address 0x%llx\t| 0x%llx\n", cur, *((Address*)cur));
		}
	}
}
