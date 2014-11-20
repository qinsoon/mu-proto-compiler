#include "runtime.h"

void freeThreadContext(void*);
void setupBootingThreadContext();

void initThread() {
    pthread_key_create(&currentUVMThread, freeThreadContext);
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
    
    pthread_setspecific(currentUVMThread, t);
}

UVMThread* getThreadContext() {
    return (UVMThread*) pthread_getspecific(currentUVMThread);
}

void freeThreadContext(void * chunk) {
    free(chunk);
}