#include "runtime.h"
#include <stdio.h>
#include <sys/mman.h>
#include <stdlib.h>

// heap include an immix space (blocks) and a free list space.

Address heapStart;

ImmixSpace* immixSpace;
FreeListSpace* largeObjectSpace;

Word markState = OBJECT_HEADER_MARK_STATE_BASE_VALUE;

FreeListSpace* newFreeListSpace();

void initHeap() {
    DEBUG_PRINT(1, ("initializing heap..\n"));
    
    heapStart = (Address) mmap(0, HEAP_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED | MAP_ANON, -1, 0);
    DEBUG_PRINT(1, ("-heap start at 0x%llx\n", heapStart));
    
    Address immixSpaceStart = alignUp(heapStart, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(1, ("-immix space start at 0x%llx (aligned to %d)\n", immixSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(heapStart, immixSpaceStart);
    
    Address endOfImmixSpace = immixSpaceStart + (HEAP_SIZE * HEAP_IMMIX_FRACTION);
    Address freelistSpaceStart = alignUp(endOfImmixSpace, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(1, ("-freelist space start at 0x%llx (aligned to %d)\n", freelistSpaceStart, IMMIX_BYTES_IN_BLOCK));	// not using this "freelist space"
    fillAlignmentGap(endOfImmixSpace, freelistSpaceStart);
    
    immixSpace = newImmixSpace(immixSpaceStart, freelistSpaceStart);
    largeObjectSpace = newFreeListSpace();

    DEBUG_PRINT(1, ("-init object map\n"));
    initObjectMap();
}

extern Address ImmixMutator_alloc(ImmixMutator* mutator, int64_t size, int64_t align);

Address allocObj(int64_t size, int64_t align) {
    DEBUG_PRINT(5, ("========Calling on allocObj========\n"));
                
    ImmixMutator* mutator = &(getThreadContext()->_mutator);
    DEBUG_PRINT(5, ("pthread=%llx, mutator=%llx\n", (Address)pthread_self(), (Address)mutator));
    
    return ImmixMutator_alloc(mutator, size, align);
}

/*
 * this method needs to cooperate with the object model 
 * implemented in uVM compiler (currently uvm.objectmodel.SimpleObjectModel)
 */
void initObj(Address addr, uint64_t header) {
    DEBUG_PRINT(5, ("========Calling on initObj======== \n"));
    DEBUG_PRINT(5, ("addr=0x%llx, header=0x%llx\n", addr, header));

    uVM_assert(isObjectStart(addr) || isLargeObjectStart(addr), "not writing into header in initObj()");
    *((uint64_t*) addr) = newObjectHeaderWithMarkBit(header, OBJECT_HEADER_MARK_BIT_MASK, markState);
}

void triggerGC() {
    // enable yieldpoint
    turnOnYieldpoints();
    
    // inform collector controller (it will ensure all threads are blocked)
    wakeCollectorController();
    
    // make current thread wait
    UVMThread* cur = getThreadContext();
    cur->_block_status = NEED_TO_BLOCK;
    yieldpoint();
    
    // the thread won't reach here until GC is done
}

void spaceInfo() {
	printf("Immix Space       : used = %lld bytes\n", immixSpace->used);
	printf("Large Object Space: used = %lld bytes\n", largeObjectSpace->used);
}

bool isInImmixSpace(Address addr) {
	if (immixSpace->immixStart <= addr && immixSpace->freelistStart >= addr)
		return true;
	else return false;
}

bool isInLargeObjectSpace(Address addr) {
	FreeListNode* node = largeObjectSpace->head;
	FreeListNode* cur;
	for (cur = node; cur != NULL; cur = cur->next) {
		if (cur->addr <= addr && cur->addr + cur->size >= addr)
			return true;
	}
	return false;
}

bool isLargeObjectStart(Address addr) {
	FreeListNode* node = largeObjectSpace->head;
	FreeListNode* cur;
	for (cur = node; cur != NULL; cur = cur->next) {
		if (cur->addr == addr)
			return true;
	}
	return false;
}

Address getLargeObjectStart(Address addr) {
	FreeListNode* node = largeObjectSpace->head;
	FreeListNode* cur;
	for (cur = node; cur != NULL; cur = cur->next) {
		if (cur->addr <= addr && cur->addr + cur->size >= addr)
			return cur->addr;
	}
	return (Address) NULL;
}

/*
 * freelist space (now using global malloc)
 */
FreeListSpace* newFreeListSpace() {
	FreeListSpace* ret = (FreeListSpace*) malloc(sizeof(FreeListSpace));

	ret->head = NULL;
	ret->last = NULL;
	ret->used = 0;
	pthread_mutex_init(&(ret->lock), NULL);

	return ret;
}

Address allocLarge(FreeListSpace* flSpace, int64_t size, int64_t align) {
	DEBUG_PRINT(3, ("acquiring from global memory (allocLarge(), size=%lld, align=%lld)\n", size, align));
	pthread_mutex_lock( &(flSpace->lock));

	// actually allocation
	Address addr;
	int ret = posix_memalign((void*)&addr, align, size);
	if (ret != 0) {
		printf("trying to alloc from freelist space: size=%lld, align=%lld\n", size, align);
		uVM_fail("failed posix_memalign alloc");
	}
//	Address addr = (Address) malloc(size);

	// metadata
	FreeListNode* node = (FreeListNode*) malloc(sizeof(FreeListNode));
	node->next = NULL;
	node->addr = addr;
	node->size = size;

	// update freelist space
	if (flSpace->last == NULL) {
		// first node
		flSpace->head = node;
		flSpace->last = node;
	} else {
		flSpace->last->next = node;
	}
	flSpace->used = flSpace->used + size;

	pthread_mutex_unlock( &(flSpace->lock));

	return addr;
}

void FreeListSpace_release(FreeListSpace* space) {
	FreeListNode* cur = space->head;
	FreeListNode* prev = NULL;

	while(cur != NULL) {
		if (!testMarkBitInHeader(cur->addr, OBJECT_HEADER_MARK_BIT_MASK, markState)) {
			// free the memory
			free((void*)cur->addr);

			// remove the node from link list
			if (prev == NULL) {
				space->head = cur->next;
			} else {
				prev->next = cur->next;
			}

			// free the node
			free(cur);
		}

		// proceed to next node
		prev = cur;
		cur = cur->next;
	}
}

/**
 * object map related - for immix space
 */
ObjectMap* objectMap;

void initObjectMap() {
	int64_t immixSpaceSize = immixSpace->freelistStart - immixSpace->immixStart;
	// as we use 1 bit for WORD_SIZE in the immix space
	int objectMapSize = (immixSpaceSize / WORD_SIZE);
	int objectMapSizeInBytes = objectMapSize / 8;

	printf("object map size (in bytes)=%d\n", objectMapSizeInBytes);
	printf("we will need a %d length bitmap\n", objectMapSize);

	objectMap = (ObjectMap*) malloc(sizeof(ObjectMap) + objectMapSizeInBytes);

	printf("BITS_PER_WORD = %lld \n", BITS_PER_WORD);

	// init
	objectMap->start = immixSpace->immixStart;
	objectMap->bitmapSize = objectMapSize;
	memset(objectMap->bitmap, 0, objectMapSizeInBytes);
}

Address objectMapIndexToAddress(int i) {
	uVM_assert(i >= 0 && i <= objectMap->bitmapSize, "index out of bounds in objectMapIndexToAddress()");
	return objectMap->start + i * WORD_SIZE;
}
int addressToObjectMapIndex(Address addr) {
	uVM_assert((addr - objectMap->start) % WORD_SIZE == 0, "addr is not aligned in addressToObjectMapIndex()");
	int ret = (addr - objectMap->start) / WORD_SIZE;
	uVM_assert(ret <= objectMap->bitmapSize, "index out of bounds in addressToObjectMapIndex()");
	return ret;
}

//int objectsMarked = 0;
void markInObjectMap(Address ref) {
	int bitI = addressToObjectMapIndex(ref);
	if (bitI > objectMap->bitmapSize)
		uVM_fail("exceeding object map size");

	set_bit(objectMap->bitmap, bitI);
//	objectsMarked ++;
//	printf("Obj %llx marked at %d\n", ref, bitI);
}

void clearRangeInObjectMap(Address lineStart, int size) {
	for (int i = addressToObjectMapIndex(lineStart); i < addressToObjectMapIndex(lineStart + size); i++) {
		clear_bit(objectMap->bitmap, i);
	}
}

extern int typeCount;
Address getObjectStart(Address iref) {
	if (isObjectStart(iref))
		return iref;

	// iref could a valid iref or not an iref at all
	int bitI = addressToObjectMapIndex(iref);
	for (; bitI >= 0; bitI--) {
//		printf("bitI = %d\n", bitI);
		if (get_bit(objectMap->bitmap, bitI) != 0) {
			Address potentialObjectStart = objectMapIndexToAddress(bitI);

//			printf("***bitI = %d***\n", bitI);
//			printf("***iref = %llx, potential ref=%llx***\n", iref, potentialObjectStart);
//			printf("***header = %llx***\n", *((uint64_t*)potentialObjectStart));
			printObject(potentialObjectStart);

			int typeId = getTypeID(potentialObjectStart);

//			printf("***typeID = %d***\n", typeId);
			if (typeId >= typeCount && typeId < 0)
				return (Address) NULL;

			TypeInfo* typeInfo = getTypeInfo(potentialObjectStart);
			int64_t objSize = typeInfo->size;

//			printf("***type size = %lld***\n", objSize);
//			printf("***offset = %lld***\n", offset);
			// check if iref is within the object size
			if ((iref - potentialObjectStart) <= objSize)
				return potentialObjectStart;
			else {
				// it is not an iref
				return (Address) NULL;
			}
		}
	}

	return (Address) NULL;
}

Address findBaseRef(Address candidate) {
	if (isInImmixSpace(candidate)) {
		if (isObjectStart(candidate)) {
			return candidate;
		} else {
			return getObjectStart(candidate);
		}
	} else if (isInLargeObjectSpace(candidate)) {
		if (isLargeObjectStart(candidate)) {
			return candidate;
		} else {
			return getLargeObjectStart(candidate);
		}
	} else {
		return (Address) NULL;
	}
}

bool isObjectStart(Address ref) {
	if (ref < immixSpace->immixStart || ref > immixSpace->freelistStart)
		return false;

	int bitI = addressToObjectMapIndex(ref);
	return get_bit(objectMap->bitmap, bitI) != 0;
}

Address alignUp(Address region, int align) {
	if (align != 2 && align != 4 && align != 8 && align % 8 != 0) {
		printf("alignUp(), align=%d\n", align);
		uVM_fail("possibly wrong align in alignUp()");
	}

    return (region + align - 1) & ~ (align - 1);
}

void fillAlignmentGap(Address start, Address end) {
	if (end <= start)
		return;

    memset((void*)start, ALIGNMENT_VALUE, end - start);
}

Word newObjectHeaderWithMarkBit(Word oldHeader, Word mask, Word markState) {
	return oldHeader | ((~markState) & mask);
}

void setMarkBitInHeader(Address ref, Word mask, Word markState) {
	Word hdr = *((Word*) ref);
	*((Word*) ref) = hdr ^ ((hdr ^ markState) & mask);
}

bool testMarkBitInHeader(Address ref,    Word mask, Word markState) {
	return ((*((Word*) ref)) ^ ~markState) & mask;
}

void flipBit(Word mask, Word* markState) {
	*markState = *markState ^ mask;
}

void setMaskedBitInHeader  (Address ref, Word mask) {
	*((Word*)ref) |= mask;
}

bool testMaskedBitInHeader (Address ref, Word mask) {
	return (*((Word*)ref) & mask) != 0;
}

void clearMaskedBitInHeader(Address ref, Word mask) {
	*((Word*)ref) &= ~mask;
}
