#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <stdint.h>
#include <sys/mman.h>
#include <unistd.h>

typedef uint64_t Address;

// *** yieldpoint ***

// only define one of the two
#define CHECKING_YIELDPOINT
//#define PAGE_PROTECTION_YIELDPOINT

#ifdef CHECKING_YIELDPOINT
extern int64_t yieldpoint_check;
#endif

#ifdef PAGE_PROTECTION_YIELDPOINT
extern Address yieldpoint_protect_page;
#endif

#define LOG_BYTES_IN_PAGE 12
#define BYTES_IN_PAGE (1 << LOG_BYTES_IN_PAGE)

// *** heap general ***
extern Address heapStart;

#define HEAP_SIZE (50 << 20)
#define HEAP_IMMIX_FRACTION 0.7
#define HEAP_FREELIST_FRACTION 0.3

#define ALIGNMENT_VALUE 9

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

struct FreeListNode;
typedef struct FreeListNode {
	struct FreeListNode* next;
	Address addr;
} FreeListNode;

typedef struct FreeListSpace {
	FreeListNode* head;
	FreeListNode* last;

	pthread_mutex_t lock;

	int64_t used;
} FreeListSpace;

typedef struct ImmixCollector {
    
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
 * THREAD
 */

typedef enum {INIT, RUNNING, NEED_TO_BLOCK, BLOCKED} block_t;

struct UVMThread;
typedef struct UVMStack {
	int64_t stackSlot;
    Address _sp;
    Address _bp;
    Address _ip;
    int64_t stackSize;
    void *(*entry_func)(void*);

    Address lowerBound;
    Address upperBound;

    struct UVMThread* thread;
} UVMStack;

extern int UVMStackMetaSize;

// this constant should match Java part implementation
#define STACK_SIZE		65535

#define MAX_STACK_COUNT 65535
extern UVMStack* uvmStacks[MAX_STACK_COUNT];
extern int stackCount;

extern Address newThread(Address stack);
extern Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args);
extern void addNewStack(UVMStack* stack);
extern UVMStack* getCurrentStack();
extern void printStackInfo(UVMStack* stack);
extern void inspectStack(UVMStack* stack, int64_t max);

typedef struct UVMThread {
    int threadSlot;
    
    // internal pthread
    pthread_t _pthread;
    
    pthread_mutex_t _mutex;
    pthread_cond_t  _cond;
    
    block_t _block_status;
    
    // for garbage collection
    ImmixMutator _mutator;

    struct UVMThread* stack;
} UVMThread;

#define MAX_THREAD_COUNT 1024
extern UVMThread* uvmThreads[MAX_THREAD_COUNT];
extern int threadCount;

extern void addNewThread(UVMThread* thread);

extern void threadExit();

/*
 * create thread context and put it in local
 */
extern UVMThread* getThreadContext();

/*
 * block and unblock on pthread cond
 */
void block(UVMThread* uvmThread);
void unblock(UVMThread* uvmThread);

/*
 * FUNCTIONS
 */
extern void initRuntime();
extern void initThread();
extern void initHeap();
extern void initCollector();
extern void initStack();

extern pthread_key_t currentUVMThread;

/*
 * new mutator context
 */
extern ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space);
extern ImmixSpace* newImmixSpace(Address, Address);
extern FreeListSpace* newFreeListSpace();

extern ImmixSpace* immixSpace;
extern FreeListSpace* largeObjectSpace;

/*
 * higher verbose level, more detailed output
 * 5 - log everything
 * 3 - global allocation, yieldpoint synchronization
 * 1 - initilizer
 */
#define DEBUG_VERBOSE_LEVEL 3
#define DEBUG

#ifdef DEBUG
# define DEBUG_PRINT(l, x) if (DEBUG_VERBOSE_LEVEL >= l) printf x
#else
# define DEBUG_PRINT(l, x) do {} while (0)
#endif

extern void uvmPrintInt64(int64_t);
extern void uvmPrintDouble(double);
extern void uvmPrintStr(Address);

/*
 * MEMORY
 */

extern ImmixMutator* ImmixMutator_reset(ImmixMutator* m);

// alloc
extern Address allocObj(int64_t size, int64_t align);
extern void initObj(Address addr, uint64_t header);

// Global
typedef enum {MUTATOR, BLOCKING_FOR_GC, BLOCKED_FOR_GC, GC} GCPhase_t;
extern GCPhase_t phase;

extern void triggerGC();
extern void wakeCollectorController();

extern Address alignUp(Address region, int align);
extern void fillAlignmentGap(Address start, Address end);

/*
 * MISC
 */

extern void yieldpoint();

extern void uvmMainExit(int64_t);

extern void turnOffYieldpoints();
extern void turnOnYieldpoints();
extern void uVM_fail(const char* str);
extern void NOT_REACHED();
