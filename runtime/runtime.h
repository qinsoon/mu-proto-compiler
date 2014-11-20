#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <stdint.h>
#include <sys/mman.h>

typedef uint64_t Address;

// general
Address heapStart;

#define HEAP_SIZE (500 << 20)
#define HEAP_IMMIX_FRACTION 0.7
#define HEAP_FREELIST_FRACTION 0.3

#define ALIGNMENT_VALUE 9

// immix

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
} ImmixSpace;

typedef struct ImmixCollector {
    
} ImmixCollector;

typedef struct ImmixMutator {
    ImmixSpace* globalSpace;
    
    Address cursor;
    Address limit;
    int line;
    
    Address blockStart;
    Address blockEnd;
    
    uint8_t* lineMark;


} ImmixMutator;

/*
 * THREAD
 */

typedef struct UVMThread {
    // internal pthread
    pthread_t _pthread;
    
    // for garbage collection
    ImmixMutator _mutator;
    ImmixCollector _collector;
} UVMThread;

/*
 * FUNCTIONS
 */

extern void initRuntime();
extern void initThread();
extern void initHeap();

pthread_key_t currentUVMThread;

/*
 * new mutator context
 */
extern ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space);
extern ImmixSpace* newSpace(Address, Address);

ImmixSpace* immixSpace;

/*
 * create thread context and put it in local
 */
extern void setupThreadContext();
extern UVMThread* getThreadContext();

#define DEBUG

#ifdef DEBUG
# define DEBUG_PRINT(x) printf x
#else
# define DEBUG_PRINT(x) do {} while (0)
#endif

/*
 * MEMORY
 */

extern Address alignUp(Address region, int align);
extern void fillAlignmentGap(Address start, Address end);

/*
 * MISC
 */

extern void uVM_fail(const char* str);