#ifndef TYPEINFO_H
#define TYPEINFO_H

#include "runtimetypes.h"

// ---------------------TYPES------------------------

/*
 * type information
 *
 * compiler.phase.RuntimeCodeEmission will generate code that
 * uses the type information. Make sure the code here complies
 * with the Java code.
 *
 */
typedef struct TypeInfo {
	int64_t id;

	int64_t size;
	int64_t align;

	// for arrays
	int64_t eleSize;
	int64_t length;

	// offsets
	int64_t nFixedRefOffsets;
	int64_t nFixedIRefOffsets;
//	int64_t nVarRefOffsets;
//	int64_t nVarIRefOffsets;
	int64_t refOffsets[1];	// fixed ref offsets, fixed iref offsets,
							// var ref offsets, var iref offsets
} TypeInfo;

// ---------------------FUNCTIONS------------------------

extern TypeInfo* allocScalarTypeInfo(int64_t id, int64_t size, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets);
extern TypeInfo* allocArrayTypeInfo (int64_t id, int64_t eleSize, int64_t length, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets);
//extern TypeInfo* allocHybridTypeInfo(int64_t id, int64_t size, int64_t align, int64_t eleSize, int64_t length, int64_t nFixedRefOffsets, int64_t nFixedIRefOffsets, int64_t nVarRefOffsets, int64_t nVarIRefOffsets);

extern int getTypeID(Address ref);
extern TypeInfo* getTypeInfo(Address ref);

extern void printObject(Address ref);

#endif
