#include <stdlib.h>
#include "runtime.h"

UVMStack* uvmStacks[MAX_STACK_COUNT];
int stackCount = 0;
pthread_mutex_t stackAcctLock;

int UVMStackMetaSize = sizeof(UVMStack);

void initStack() {
	int i = 0;
	for (; i < MAX_STACK_COUNT; i++)
		uvmStacks[i] = NULL;

	pthread_mutex_init(&stackAcctLock, NULL);
}

UVMStack* getCurrentStack() {
	UVMThread* t = getThreadContext();
	return (UVMStack*) t->stack;
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
    printf("slot=%lld\n", s->stackSlot);
    printf("range=%llx\n", s->lowerBound);
    printf("     ~%llx\n", s->upperBound);
    printf("   sp=%llx\n", s->_sp);
    printf("   bp=%llx\n", s->_bp);
    printf("   ip=%llx\n", s->_ip);
    printf("stackSize=%lld\n", s->stackSize);
    printf("entryFunc=%p\n", s->entry_func);
    printf("------------------\n");
}

void inspectStack(UVMStack* stack, int64_t maxValues) {
	Address stackTop = stack->upperBound;
	Address cur;

	printf("STACK STARTS (hi to lo, printing %lld values)\n", maxValues);
	printf("     -------------------\n");
	for (cur = stackTop - 8; cur >= stack->lowerBound && stackTop - cur < maxValues*8; cur -= 8) {
		printf("     0x%llx | %llx \n", cur, *((uint64_t*)cur));
	}
	printf("     ...\n");
	printf("     -------------------\n");
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
    stackStruct->lowerBound = ret + sizeof(UVMStack);
    stackStruct->upperBound = stackStruct->_sp;

    // inform the runtime about the new stack
    addNewStack(stackStruct);

    printStackInfo(stackStruct);

    return ret;
}
