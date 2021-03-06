#include "runtime.h"
#include <stdlib.h>
#include <stdio.h>

#define TRACE_BLOCK true

int64_t yieldpoint_check;
Address yieldpoint_protect_page;

UVMThread* uvmThreads[MAX_THREAD_COUNT];
int threadCount = 0;
pthread_mutex_t threadAcctLock;

pthread_key_t currentUVMThread;

void* freeThreadContext(void* a);
void setupBootingThreadContext();

void initThread() {
    // init all threads
    int i = 0;
    for (; i < MAX_THREAD_COUNT; i++)
        uvmThreads[i] = NULL;
    
    // init lock
    pthread_mutex_init(&threadAcctLock, NULL);

    // create thread local storage
    pthread_key_create(&currentUVMThread, NULL);
    
    // set up booting thread
    // setupBootingThreadContext();
}

void addNewThread(UVMThread* thread) {
	pthread_mutex_lock(&threadAcctLock);

	uvmThreads[threadCount] = thread;
	thread->threadSlot = threadCount;
	threadCount++;

	if (threadCount >= MAX_THREAD_COUNT) {
		uVM_fail("exceed MAX_THREAD_COUNT: need implementation here");
	}

	pthread_mutex_unlock(&threadAcctLock);
}

void initUVMThread(UVMThread* thread) {
	pthread_mutex_init(&(thread->_mutex), NULL);
	pthread_cond_init(&(thread->_cond), NULL);
	thread->_block_status = INIT;
	ImmixMutator_init(&(thread->_mutator), immixSpace);

	addNewThread(thread);
}

void setupBootingThreadContext() {
    UVMThread *t = (UVMThread*) malloc(sizeof(UVMThread));
    t->_pthread = pthread_self();
    initUVMThread(t);
    
    pthread_setspecific(currentUVMThread, t);
}

UVMThread* getThreadContext() {
    UVMThread* ret = (UVMThread*) pthread_getspecific(currentUVMThread);
    DEBUG_PRINT(5, ("thread context (UVMThread) = %llx\n", (Address) ret));
    return ret;
}

void* freeThreadContext(void* a) {
	UVMThread* t = (UVMThread*)a;

    pthread_mutex_destroy(&(t->_mutex));
    pthread_cond_destroy(&(t->_cond));

	pthread_mutex_lock(&threadAcctLock);
	uvmThreads[t->threadSlot] = NULL;
	pthread_mutex_unlock(&threadAcctLock);

	free((void*)t);

	return NULL;
}

void block(UVMThread* uvmThread) {
    if (TRACE_BLOCK)
        DEBUG_PRINT(3, ("Thread%p is about to block\n", uvmThread->_pthread));
    uvmThread->_block_status = BLOCKED;
    
    pthread_mutex_lock(&(uvmThread->_mutex));
    while (uvmThread->_block_status == BLOCKED) {
        pthread_cond_wait(&(uvmThread->_cond), &(uvmThread->_mutex));
    }
    pthread_mutex_unlock(&(uvmThread->_mutex));
    
    if (TRACE_BLOCK)
        DEBUG_PRINT(3, ("Thread%p finished block\n", uvmThread->_pthread));
}

void unblock(UVMThread* uvmThread) {
    if (TRACE_BLOCK)
        DEBUG_PRINT(3, ("Thread%p is about to unblock\n", uvmThread->_pthread));
    
    uvmThread->_block_status = RUNNING;
    
    pthread_mutex_lock(&(uvmThread->_mutex));
    pthread_cond_signal(&(uvmThread->_cond));
    pthread_mutex_unlock(&(uvmThread->_mutex));
}

void printThreadInfo(UVMThread* t) {
	printf("----THREAD INFO (%p)----\n", t);
	printf("slot=%d\n", t->threadSlot);
	printf("pthread=%p\n", (void*)t->_pthread);
	printf("block stat=%d\n", t->_block_status);
	printf("stack=%p\n", t->stack);
    printf("------------------\n");
}

void* uVMThreadLaunch(void* a) {
	UVMStack* stack = (UVMStack*) a;
	UVMThread* thread = stack->thread;
	void* (*entry)() = stack->entry_func;
	DEBUG_PRINT(3, ("uVMThreadLaunch: stack=%p, thread=%p\n", (void*) stack, (void*) thread));
	DEBUG_PRINT(3, ("                 entry func=%p\n", (void*) (Address) entry));

	DEBUG_PRINT(3, ("about to invoke entry function...\n"));

	// associate the stack with the thread
	stack->thread = thread;
	thread->stack = stack;

	// set current uvmthread key (TLS)
	pthread_setspecific(currentUVMThread, thread);

	// change stack (RBP) to nominated address
	void* saved_rsp;
//	void* saved_rbp;

	__asm__(
			// save rsp
			"mov %%rsp, %0		\n"
			// change rbp, rsp
			"mov %1, %%rsp		\n"
			// pop all parameters registers
			// xmm first
			"movsd    0(%%rsp), %%xmm7	\n"
			"movsd    8(%%rsp), %%xmm6	\n"
			"movsd   16(%%rsp), %%xmm5	\n"
			"movsd   24(%%rsp), %%xmm4	\n"
			"movsd   32(%%rsp), %%xmm3	\n"
			"movsd   40(%%rsp), %%xmm2	\n"
			"movsd   48(%%rsp), %%xmm1	\n"
			"movsd   56(%%rsp), %%xmm0	\n"
			// mov stack pointer
			"add $64, %%rsp		\n"
//			// gpr
			"popq %%r9			\n"
			"popq %%r8			\n"
			"popq %%rcx			\n"
			"popq %%rdx			\n"
			"popq %%rsi			\n"
			"popq %%rdi			\n"
			// call entry function
			"jmp *%2			\n"

			: "=rm" (saved_rsp)
			: "rm" (stack->_sp),
			  "rm" (entry)
			: "memory", "rsp",
			  "xmm7", "xmm6", "xmm5", "xmm4", "xmm3", "xmm2", "xmm1", "xmm0",
			  "r9", "r8", "rcx", "rdx", "rsi", "rdi"
	);

	DEBUG_PRINT(3, ("new thread finished, returned to uVMThreadLaunch (SHOULD NOT REACH HERE)\n"));
	NOT_REACHED();
	return NULL;
}

void threadExit() {
	UVMThread* uvmT = getThreadContext();
	if (uvmT != NULL) {
		DEBUG_PRINT(3, ("Thread%d (%p) exits\n", uvmT->threadSlot, (void*) uvmT));
		freeThreadContext(uvmT);
	}
	pthread_exit(NULL);
}

Address newThread(Address stack) {
    DEBUG_PRINT(3, ("Create new thread for stack %llx\n", stack));
    printStackInfo((UVMStack*)stack);

    UVMThread* t = (UVMThread*) malloc(sizeof(UVMThread));
    initUVMThread(t);

    ((UVMStack*) stack) -> thread = t;	// so we will be able to find the UVMThread in the new thread

    pthread_create(&(t->_pthread), NULL, uVMThreadLaunch, (void*) stack);

    return (Address) t;
}

void yieldpoint() {
    UVMThread* t = getThreadContext();

    DEBUG_PRINT(3, ("Thread%p reaches a yieldpoint\n", t->_pthread));

    // if we need to block
    if (t->_block_status == NEED_TO_BLOCK) {
    	// save rsp first
    	void* rsp;
    	void* rbp;

    	__asm__(
    			"mov %%rsp, %0		\n"
    			"mov %%rbp, %1		\n"
    			: "=rm" (rsp),
				  "=rm" (rbp)
		);
    	t->stack->_sp = (Address) rsp;
    	t->stack->_bp = (Address) rbp;

        block(t);
    }
}

void turnOffYieldpoints() {
    // checking
#ifdef CHECKING_YIELDPOINT
    yieldpoint_check = 0;
#endif

    // page protection
#ifdef PAGE_PROTECTION_YIELDPOINT
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_WRITE);
#endif
}

void turnOnYieldpoints() {
    // checking
#ifdef CHECKING_YIELDPOINT
    yieldpoint_check = 1;
#endif

    // page protection
#ifdef PAGE_PROTECTION_YIELDPOINT
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_NONE);
#endif
}
