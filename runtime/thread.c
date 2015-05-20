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
    setupBootingThreadContext();
}

void addNewThread(UVMThread* thread) {
	pthread_mutex_lock(&threadAcctLock);

	uvmThreads[threadCount] = thread;
	thread->threadSlot = threadCount;
	threadCount++;

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
	DEBUG_PRINT(3, ("uVMThreadLaunch: stack=%p, thread=%p\n", stack, thread));

	// set current uvmthread key (TLS)
	pthread_setspecific(currentUVMThread, thread);

	// change stack (RBP) to nominated address

	// fake a frame for this function (uVMThreadLaunch)

	// call the actual entry function

	return NULL;
}

Address newThread(Address stack) {
    DEBUG_PRINT(3, ("Create new thread for stack %llx\n", stack));

    UVMThread* t = (UVMThread*) malloc(sizeof(UVMThread));
    initUVMThread(t);

    ((UVMStack*) stack) -> thread = t;	// so we will be able to find the UVMThread in the new thread

    pthread_create(&(t->_pthread), NULL, uVMThreadLaunch, (void*) stack);

    return (Address) t;
}
