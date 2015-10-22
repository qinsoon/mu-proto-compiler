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

/*
 * Stack unwind info - target specific
 */
typedef struct X64CallerSavedRegisterOffsets {
	int64_t rax, rcx, rdx, rdi, rsi;
	int64_t r8, r9, r10, r11;
	int64_t rsp, rip;

	int64_t xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7;
	int64_t xmm8, xmm9, xmm10, xmm11, xmm12, xmm13, xmm14, xmm15;
} X64RegisterOffsets;

typedef struct X64CallsiteInfo {
	Address returnAddress;	// the address of the instruction immediately after the call
							// can be compared with RIP from the prev frame
	struct X64CallerSavedRegisterOffsets callerSavedRegs;
	Address landingPad;
} X64CallsiteInfo;

typedef struct X64CalleeSavedRegisterOffsets {
	int64_t rbx, rbp, r12, r13, r14, r15;
} X64CalleeSavedRegisterOffsets;

typedef struct UnwindTable {
	int64_t funcID;
	struct X64CalleeSavedRegisterOffsets calleeSavedRegs;

	int callsitesN;
	struct X64CallsiteInfo callsites[1];
} UnwindTable;

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

extern UnwindTable** unwindTable;
extern int unwindTableCount;

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
 * Exception
 */
extern void throwException(Address exceptionObj);
extern Address landingPad();
extern UnwindTable* allocateUnwindTable(int64_t callsites);

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
