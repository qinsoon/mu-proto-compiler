#ifndef HEAP_H
#define HEAP_H

#include "runtimetypes.h"
#include <pthread.h>
//#include <sys/mman.h>

#define LOG_BYTES_IN_PAGE 12
#define BYTES_IN_PAGE (1 << LOG_BYTES_IN_PAGE)

// *** heap general ***
extern Address heapStart;

//#define HEAP_SIZE (1 << 20)
#define HEAP_SIZE (1 << 19)		// 512Kb
#define HEAP_IMMIX_FRACTION 0.7
#define HEAP_FREELIST_FRACTION 0.3

#define ALIGNMENT_VALUE 9

#define OBJECT_HEADER_SIZE 8

// *** immix ***

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

typedef struct ImmixBlock {
    struct ImmixBlock* next;
    struct ImmixBlock* prev;

    uint8_t state;
    Address start;
    uint8_t* lineMarkTable;
} ImmixBlock;

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
 * object map for immix space
 */
typedef struct ObjectMap {
	Address start;

	int64_t bitmapSize;		// how many bits
	int64_t bitmapUsed;
	Word bitmap[];
} ObjectMap;

extern ObjectMap* objectMap;

extern void initObjectMap();
extern void markInObjectMap(Address ref);

extern Address objectMapIndexToAddress(int i);
extern int addressToObjectMapIndex(Address addr);

extern bool isObjectStart(Address ref);
extern Address getObjectStart(Address ref);

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

typedef struct ImmixCollector {
    int64_t placeholder;		// so gcc doesnt complain
} ImmixCollector;

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
 * new mutator context
 */
extern ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space);
extern ImmixSpace* newImmixSpace(Address, Address);
extern FreeListSpace* newFreeListSpace();

extern ImmixSpace* immixSpace;
extern FreeListSpace* largeObjectSpace;

extern ImmixMutator* ImmixMutator_reset(ImmixMutator* m);

// alloc
extern Address allocObj(int64_t size, int64_t align);
extern void initObj(Address addr, uint64_t header);

/*
 * MEMORY
 */

// Global
typedef enum {MUTATOR, BLOCKING_FOR_GC, BLOCKED_FOR_GC, GC} GCPhase_t;
extern GCPhase_t phase;

extern bool isInImmixSpace(Address addr);
extern bool isInLargeObjectSpace(Address addr);
extern bool isLargeObjectStart(Address addr);
extern Address getLargeObjectStart(Address addr);

// collection
extern void triggerGC();
extern void wakeCollectorController();

extern Address findBaseRef(Address iref);

/*
 * a linked list that would be useful when scanning object
 */
struct AddressNode;
typedef struct AddressNode {
	struct AddressNode* next;
	Address addr;
} AddressNode;

extern AddressNode* pushToList(Address addr, AddressNode** list);
extern AddressNode* popFromList(AddressNode** list);

// utils

extern Address alignUp(Address region, int align);
extern void fillAlignmentGap(Address start, Address end);

#endif
