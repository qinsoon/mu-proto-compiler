#ifndef THREADSTACK_H
#define THREADSTACK_H

#include <pthread.h>
#include "runtimetypes.h"
#include "heap.h"

// ---------------------TYPES------------------------

/*
 * Thread block status
 */
typedef enum {INIT, RUNNING, NEED_TO_BLOCK, BLOCKED} block_t;

/*
 * UVMStack
 */
struct UVMThread;
typedef struct UVMStack {
	int64_t stackSlot;
    Address _sp;
    Address _bp;
    Address _ip;
    int64_t stackSize;
    void *(*entry_func)(void*);

    //     | overflow guard page | actual stack ..................... | underflow guard page|
    //     |                     |                                    |                     |
    // overflowGuard           lowerBound                           upperBound
    //                                                              underflowGuard
    Address lowerBound;
    Address overflowGuard;
    Address upperBound;
    Address underflowGuard;

    struct UVMThread* thread;
} UVMStack;

/*
 * UVMThread
 */
typedef struct UVMThread {
    int threadSlot;

    // internal pthread
    pthread_t _pthread;

    pthread_mutex_t _mutex;
    pthread_cond_t  _cond;

    block_t _block_status;

    // for garbage collection
    ImmixMutator _mutator;

    struct UVMStack* stack;
} UVMThread;

// ---------------------CONSTANTS------------------------

#define UVMStackMetaSize sizeof(UVMStack)

// this constant should match Java part implementation, see uvm.type.Stack
#define STACK_SIZE		(4 << 20)

#define MAX_STACK_COUNT 65535
#define MAX_THREAD_COUNT 1024

// ---------------------GLOBALS------------------------

/*
 * Yieldpoints
 */
#define CHECKING_YIELDPOINT
//#define PAGE_PROTECTION_YIELDPOINT

#ifdef CHECKING_YIELDPOINT
extern int64_t yieldpoint_check;
#endif
#ifdef PAGE_PROTECTION_YIELDPOINT
extern Address yieldpoint_protect_page;
#endif

/*
 * Stacks
 */
extern UVMStack* uvmStacks[MAX_STACK_COUNT];
extern int stackCount;

/*
 * Threads
 */
extern UVMThread* uvmThreads[MAX_THREAD_COUNT];
extern int threadCount;
extern pthread_key_t currentUVMThread;

// ---------------------FUNCTIONS------------------------

/*
 * Stacks
 */
extern Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args);
extern void addNewStack(UVMStack* stack);
extern UVMStack* getCurrentStack();

extern void printStackInfo(UVMStack* stack);
extern void inspectStack(UVMStack* stack, int64_t max);

extern void scanStackForRoots(UVMStack* stack, AddressNode** roots);

/*
 * Threads
 */
extern Address newThread(Address stack);	// naming issue - should be the same as 'allocStack'
extern void addNewThread(UVMThread* thread);
extern UVMThread* getThreadContext();

extern void printThreadInfo(UVMThread* t);

void block(UVMThread* uvmThread);
void unblock(UVMThread* uvmThread);

extern void threadExit();
extern void uvmMainExit(int64_t);

/*
 * Yieldpoints
 */
extern void yieldpoint();
extern void turnOffYieldpoints();
extern void turnOnYieldpoints();

#endif
