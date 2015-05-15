#include "runtime.h"

#define TRACE_BLOCK true

void freeThreadContext(void*);
void setupBootingThreadContext();

void initThread() {
    // init all threads
    int i = 0;
    for (; i < MAX_THREAD_COUNT; i++)
        uvmThreads[i] = NULL;
    
    // create thread local storage
    pthread_key_create(&currentUVMThread, freeThreadContext);
    
    // set up booting thread
    setupBootingThreadContext();
}

void setupBootingThreadContext() {
    UVMThread *t = (UVMThread*) malloc(sizeof(UVMThread));
    t->_pthread = pthread_self();
    ImmixMutator_init(&(t->_mutator), immixSpace);
    
    uvmThreads[threadCount] = t;
    t->threadSlot = threadCount;
    threadCount++;
    
    pthread_setspecific(currentUVMThread, t);
}

UVMThread* getThreadContext() {
    UVMThread* ret = (UVMThread*) pthread_getspecific(currentUVMThread);
    DEBUG_PRINT(5, ("thread context (UVMThread) = %llx\n", (Address) ret));
    return ret;
}

void freeThreadContext(void * chunk) {
    free(chunk);
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

Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args) {
    return 0;
}

Address newThread(Address stack) {
    return 0;
}