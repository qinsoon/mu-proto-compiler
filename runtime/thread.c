#include "runtime.h"

#define TRACE_BLOCK true

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
        DEBUG_PRINT(3, ("Thread%d is about to block\n", uvmThread->threadSlot));
    uvmThread->_block_status = BLOCKED;
    
    pthread_mutex_lock(&(uvmThread->_mutex));
    while (uvmThread->_block_status == BLOCKED) {
        pthread_cond_wait(&(uvmThread->_cond), &(uvmThread->_mutex));
    }
    pthread_mutex_unlock(&(uvmThread->_mutex));
    
    if (TRACE_BLOCK)
        DEBUG_PRINT(3, ("Thread%d finished block\n", uvmThread->threadSlot));
}

void unblock(UVMThread* uvmThread) {
    if (TRACE_BLOCK)
        DEBUG_PRINT(3, ("Thread%d is about to unblock\n", uvmThread->threadSlot));
    
    uvmThread->_block_status = RUNNING;
    
    pthread_mutex_lock(&(uvmThread->_mutex));
    pthread_cond_signal(&(uvmThread->_cond));
    pthread_mutex_unlock(&(uvmThread->_mutex));
}

void* uVMThreadLaunch(void* a) {
	UVMStack* stack = (UVMStack*) a;
	UVMThread* thread = stack->thread;
	void* (*entry)() = stack->entry_func;
	DEBUG_PRINT(3, ("uVMThreadLaunch: stack=%p, thread=%p\n", (void*) stack, (void*) thread));
	DEBUG_PRINT(3, ("                 entry func=%p\n", (void*) (Address) entry));

	DEBUG_PRINT(3, ("about to invoke entry function...\n"));

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

	DEBUG_PRINT(3, ("Thread%d (%p) exits\n", uvmT->threadSlot, (void*) uvmT));
	freeThreadContext(uvmT);
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
