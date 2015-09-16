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
	printf("----STACK INFO (%p)----\n", s);
    printf("slot=%lld\n", s->stackSlot);
    printf("range\n");
    printf("      =0x%llx ~ 0x%llx\n", s->lowerBound, s->upperBound);
    printf("overflow guard\n");
    printf("      =0x%llx ~ 0x%llx\n", s->overflowGuard, s->overflowGuard + BYTES_IN_PAGE - 1);
    printf("underflow guard\n");
    printf("      =0x%llx ~ 0x%llx\n", s->underflowGuard, s->underflowGuard + BYTES_IN_PAGE - 1);
    printf("   sp=0x%llx\n", s->_sp);
    printf("   bp=0x%llx\n", s->_bp);
    printf("   ip=0x%llx\n", s->_ip);
    printf("stackSize=%lld\n", s->stackSize);
    printf("entryFunc=%p\n", (void*)(Address) s->entry_func);
    printf("------------------\n");
}

void inspectStack(UVMStack* stack, int64_t maxValues) {
	Address stackTop = stack->upperBound;
	Address cur;

	printf("STACK STARTS (hi to lo, printing %lld values)\n", maxValues);
	printf("     -------------------\n");
	for (cur = stackTop - WORD_SIZE; cur >= stack->lowerBound && stackTop - cur < maxValues*WORD_SIZE; cur -= WORD_SIZE) {
		printf("     0x%llx | %llx \n", cur, *((uint64_t*)cur));
	}
	printf("     ...\n");
	printf("     -------------------\n");
}

extern int typeCount;

void scanStackForRoots(UVMStack* stack, AddressNode* roots) {
	Address sp = stack->_sp;
	Address stackTop = stack->upperBound;
	Address curBP = stack->_bp;

	if (stackTop < sp)
		uVM_fail("sp is not under stack top");

	Address cur;

	int scannedSlots = 0;

	int frames = 0;

	int immixObjs = 0;
	int largeObjs = 0;
	int notObjs   = 0;

	int refs = 0;
	int irefs = 0;

	for (cur = sp; cur < stackTop; cur += WORD_SIZE) {
		scannedSlots ++;
		uint64_t candidate = *((uint64_t*)cur);

		if (cur == curBP) {
			printf("---finish a frame---\n");
			curBP = candidate;
			frames ++;
		}

		// check if *cur is a ref
		printf("checking value: 0x%llx at stack 0x%p\n", candidate, (void*) cur);

		if (isInImmixSpace(candidate)) {
			immixObjs ++;
			printf("  in immix space: ");

			if (isObjectStart(candidate)) {
				refs ++;
				printf("ref. \n");
			} else {
				Address ref = getObjectStart(candidate);
				if (ref == (Address) NULL) {
					immixObjs --;
					notObjs ++;
					printf("not a ref. \n");
				} else {
					irefs ++;
					printf("iref. \n");
				}
			}
		} else if (isInLargeObjectSpace(candidate)) {
			largeObjs ++;
			printf("  in large obj space:");

			if (isLargeObjectStart(candidate)) {
				refs ++;
				printf("ref. \n");
			} else {
				irefs ++;
				printf("iref. \n");
			}
		} else {
			notObjs ++;
		}
	}

	printf("Total scanned stack slots: %d, in %d frames\n", scannedSlots, frames);
	printf("1. immix space objects: %d\n", immixObjs);
	printf("   large objects: %d\n", largeObjs);
	printf("   not objects: %d\n", notObjs);
	printf("2. refs: %d\n", refs);
	printf("   irefs: %d\n", irefs);
	printf("----------------\n");
}

Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args) {
    DEBUG_PRINT(3, ("Allocate for new stack (size:%lld, entry:%p, args:%p)\n", stackSize, (void*) (Address) entry_func, args));

    Address stackMeta = (Address) malloc(sizeof(UVMStack));

    if (stackMeta == 0)
        uVM_fail("error when trying to malloc space for uvmstack meta\n");

    Address stackAddr;
    int totalSizeForStack = BYTES_IN_PAGE * 2 + stackSize;		// two guard pages + acutal stack
    int retval = posix_memalign((void*)&stackAddr, BYTES_IN_PAGE, totalSizeForStack);

    if (retval != 0) {
    	printf("trying to alloc for stack (align:%d, size:%d)\n", BYTES_IN_PAGE, totalSizeForStack);
    	uVM_fail("error when trying to memalign alloc space for uvm stack\n");
    }

    // create stack and initialize
    UVMStack* stackStruct = (UVMStack*) stackMeta;
    stackStruct->_sp = stackAddr + BYTES_IN_PAGE + stackSize;
    stackStruct->_bp = stackStruct->_sp;
    stackStruct->_ip = 0;
    stackStruct->stackSize = stackSize;
    stackStruct->entry_func = entry_func;

    stackStruct->lowerBound = stackAddr + BYTES_IN_PAGE;
    stackStruct->overflowGuard = stackAddr;
    stackStruct->upperBound = stackStruct->_sp;
    stackStruct->underflowGuard = stackStruct->_sp;

    // protect over/underflow guard page
    int mprotectRet1 = mprotect((void*) stackStruct->overflowGuard, BYTES_IN_PAGE, PROT_NONE);
    if (mprotectRet1 != 0) {
    	printf("failed to mprotect stack overflow page: %llx\n", stackStruct->overflowGuard);
    	uVM_fail("failed to mprotect stack overflow page");
    }
    int mprotectRet2 = mprotect((void*) stackStruct->underflowGuard, BYTES_IN_PAGE, PROT_NONE);
    if (mprotectRet2 != 0) {
    	printf("failed to mprotect stack underflow page: %llx\n", stackStruct->overflowGuard);
    	uVM_fail("failed to mprotect stack underflow page");
    }

    // inform the runtime about the new stack
    addNewStack(stackStruct);

    printStackInfo(stackStruct);

    return stackMeta;
}
