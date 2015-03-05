#include "runtime.h"

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

void setupThreadContext() {
    printf("havent implemented yet\n");
    exit(1);
}

void setupBootingThreadContext() {
    UVMThread *t = (UVMThread*) malloc(sizeof(UVMThread));
    t->_pthread = pthread_self();
    ImmixMutator_init(&(t->_mutator), immixSpace);
    
    uvmThread[threadCount] = t;
    threadCount++;
    
    pthread_setspecific(currentUVMThread, t);
}

UVMThread* getThreadContext() {
    UVMThread* ret = (UVMThread*) pthread_getspecific(currentUVMThread);
    DEBUG_PRINT(0, ("thread context (UVMThread) = %llx\n", (Address) ret));
    return ret;
}

void freeThreadContext(void * chunk) {
    free(chunk);
}