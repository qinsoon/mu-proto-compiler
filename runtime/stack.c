#include <stdlib.h>
#include "runtime.h"

UVMStack* uvmStacks[MAX_STACK_COUNT];
int stackCount = 0;
pthread_mutex_t stackAcctLock;

void initStack() {
	int i = 0;
	for (; i < MAX_STACK_COUNT; i++)
		uvmStacks[i] = NULL;

	pthread_mutex_init(&stackAcctLock, NULL);
}

void addNewStack(UVMStack* stack) {
	pthread_mutex_lock(&stackAcctLock);

    uvmStacks[stackCount] = stack;
    stack->stackSlot = stackCount;
    stackCount++;

    pthread_mutex_unlock(&stackAcctLock);
}

void printStackInfo(UVMStack* s) {
	printf("----STACK INFO----\n");
    printf("slot=%d\n", s->stackSlot);
    printf("sp=%llx\n", s->_sp);
    printf("bp=%llx\n", s->_bp);
    printf("ip=%llx\n", s->_ip);
    printf("stackSize=%lld\n", s->stackSize);
    printf("entryFunc=%p\n", s->entry_func);
    printf("args=%p\n", s->args);
    printf("------------------\n");
}

Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args) {
    DEBUG_PRINT(3, ("Allocate for new stack (size:%lld, entry:%p, args:%p)\n", stackSize, entry_func, args));

    size_t actualStackSize = sizeof(UVMStack) + stackSize;
    Address ret = (Address) malloc(actualStackSize);

    if (ret == 0)
        uVM_fail("error when trying to malloc space for new stack\n");

    // create stack and initialize
    UVMStack* stackStruct = (UVMStack*) ret;
    stackStruct->_sp = ret + actualStackSize;
    stackStruct->_bp = stackStruct->_sp;
    stackStruct->_ip = 0;
    stackStruct->stackSize = stackSize;
    stackStruct->entry_func = entry_func;
    stackStruct->args = args;

    // inform the runtime about the new stack
    addNewStack(stackStruct);

    printStackInfo(stackStruct);

    return ret;
}
