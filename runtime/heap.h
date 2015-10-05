#ifndef HEAP_H
#define HEAP_H

#include "runtimetypes.h"
#include <pthread.h>

// ---------------------TYPES------------------------

/*
 * ImmixBlock
 */
typedef struct ImmixBlock {
    struct ImmixBlock* next;
    struct ImmixBlock* prev;

    uint8_t state;
    Address start;
    uint8_t* lineMarkTable;
} ImmixBlock;

/*
 * ImmixSpace
 */
typedef struct ImmixSpace {
    Address immixStart;
    Address freelistStart;
    uint8_t* lineMarkTable;

    ImmixBlock* usableBlocks;
    ImmixBlock* usedBlocks;

    pthread_mutex_t lock;

    int64_t used;
} ImmixSpace;

/*
 * Object map for immix space
 */
typedef struct ObjectMap {
	Address start;

	int64_t bitmapSize;		// how many bits
	int64_t bitmapUsed;
	Word bitmap[];
} ObjectMap;

/*
 * FreeList Node, Space
 */
struct FreeListNode;
typedef struct FreeListNode {
	struct FreeListNode* next;
	Address addr;
	int64_t size;
} FreeListNode;

typedef struct FreeListSpace {
	FreeListNode* head;
	FreeListNode* last;

	pthread_mutex_t lock;

	int64_t used;
} FreeListSpace;

/*
 * Immix Mutator
 */
typedef struct ImmixMutator {
    ImmixSpace* globalSpace;
    FreeListSpace* largeObjectSpace;

    Address cursor;
    Address limit;
    int line;

    Address blockStart;
    Address blockEnd;

    uint8_t* lineMark;
} ImmixMutator;

/*
 * Collector
 */
typedef struct ImmixCollector {
    int64_t placeholder;		// so gcc doesnt complain
} ImmixCollector;

typedef enum {MUTATOR, BLOCKING_FOR_GC, BLOCKED_FOR_GC, GC} GCPhase_t;

/*
 * A linked list that would be useful when scanning object
 */
struct AddressNode;
typedef struct AddressNode {
	struct AddressNode* next;
	Address addr;
} AddressNode;

// ---------------------CONSTANTS------------------------

#define LOG_BYTES_IN_PAGE 12
#define BYTES_IN_PAGE (1 << LOG_BYTES_IN_PAGE)

//#define HEAP_SIZE (1 << 20)
#define HEAP_SIZE (1 << 19)		// 512Kb
#define HEAP_IMMIX_FRACTION 0.7
#define HEAP_FREELIST_FRACTION 0.3

#define ALIGNMENT_VALUE 9

/*
 * HEADER
 */
#define OBJECT_HEADER_SIZE 8

// if this bit is marked, the object is traced during current GC
// this bit will be cleared after GC
#define OBJECT_HEADER_MARK_BIT_MASK		0x0400000000000000L

/*
 * Immix constants
 */
#define IMMIX_LOG_BYTES_IN_BLOCK 16
#define IMMIX_BYTES_IN_BLOCK (1 << IMMIX_LOG_BYTES_IN_BLOCK)

#define IMMIX_LOG_BYTES_IN_LINE 8
#define IMMIX_BYTES_IN_LINE (1 << IMMIX_LOG_BYTES_IN_LINE)

#define IMMIX_LINES_IN_BLOCK (1 << (IMMIX_LOG_BYTES_IN_BLOCK - IMMIX_LOG_BYTES_IN_LINE))

#define IMMIX_LINE_MARK_FREE            0
#define IMMIX_LINE_MARK_LIVE            1
#define IMMIX_LINE_MARK_FRESH_ALLOC     2
#define IMMIX_LINE_MARK_CONSERV_LIVE    3

#define IMMIX_BLOCK_MARK_USABLE         0
#define IMMIX_BLOCK_MARK_FULL           1

// ---------------------GLOBALS------------------------

extern Address heapStart;
extern ObjectMap* objectMap;
extern GCPhase_t phase;
extern ImmixSpace* immixSpace;
extern FreeListSpace* largeObjectSpace;

// ---------------------FUNCTIONS------------------------

extern ImmixSpace* newImmixSpace(Address, Address);
extern FreeListSpace* newFreeListSpace();

extern ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space);
extern ImmixMutator* ImmixMutator_reset(ImmixMutator* m);

/*
 * Allocation
 */
extern Address allocObj(int64_t size, int64_t align);
extern void initObj(Address addr, uint64_t header);

/*
 * Object map, space, object start
 */
extern void initObjectMap();
extern void markInObjectMap(Address ref);

extern Address objectMapIndexToAddress(int i);
extern int addressToObjectMapIndex(Address addr);

extern bool isInImmixSpace(Address addr);
extern bool isInLargeObjectSpace(Address addr);

extern bool isObjectStart(Address ref);
extern bool isLargeObjectStart(Address addr);

extern Address getObjectStart(Address ref);
extern Address getLargeObjectStart(Address addr);

extern Address findBaseRef(Address iref);

/*
 * Collection
 */
extern void triggerGC();
extern void wakeCollectorController();

/*
 * AddressNode list operations
 */
extern AddressNode* pushToList(Address addr, AddressNode** list);
extern AddressNode* popFromList(AddressNode** list);

/*
 * Utilities
 */
extern Address alignUp(Address region, int align);
extern void fillAlignmentGap(Address start, Address end);

extern void setMaskedBitInHeader  (Address ref, Word mask);
extern bool testMaskedBitInHeader (Address ref, Word mask);
extern void clearMaskedBitInHeader(Address ref, Word mask);

#endif
