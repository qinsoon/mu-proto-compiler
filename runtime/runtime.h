#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <sys/mman.h>

typedef long Address;

extern void initRuntime();
extern void initHeap();

#define DEBUG

#ifdef DEBUG
# define DEBUG_PRINT(x) printf x
#else
# define DEBUG_PRINT(x) do {} while (0)
#endif

/*
 * MEMORY
 */

extern Address align(Address region, int align);
extern void fillAlignmentGap(Address start, Address end);

// general
Address heapStart;
Address immixSpaceStart;
Address freelistSpaceStart;

#define HEAP_SIZE (500 << 20)
#define HEAP_IMMIX_FRACTION 0.7
#define HEAP_FREELIST_FRACTION 0.3

#define ALIGNMENT_VALUE 9

// immix

#define IMMIX_LOG_BYTES_IN_BLOCK 16
#define IMMIX_BYTES_IN_BLOCK (1 << IMMIX_LOG_BYTES_IN_BLOCK)

#define IMMIX_LOG_BYTES_IN_LINE 8
#define IMMIX_BYTES_IN_LINE (1 << IMMIX_LOG_BYTES_IN_LINE)

#define IMMIX_LINES_IN_BLOCK (1 << (IMMIX_LOG_BYTES_IN_BLOCK - IMMIX_BYTES_IN_LINE))

typedef struct ImmixSpace {
    
} ImmixSpace;

typedef struct ImmixCollector {
    
} ImmixCollector;

typedef struct ImmixMutator {
    ImmixSpace* globalSpace;
    
    Address cursor;
    Address limit;
    Address largeCursor;
    Address largeLimit;
    
    Address markTable;
    Address recyclableBlock;

    int line;
    int lineUseCount;
    
    bool recyclableExhausted;
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