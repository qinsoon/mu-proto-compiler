#include "runtime.h"

#include <stdio.h>

extern void initTypeTable();

void initRuntime() {
	initSignalHandler();
    initHeap();
    initThread();
    initYieldpoint();
    initCollector();
    initStack();
    initTypeTable();
}

int64_t retval;

void uvmMainExit(int64_t r) {
	retval = r;
	threadExit();
}

void* uvmMain(void*);
int main(int c, char** args) {
	initRuntime();

	// alloc stack
	printf("uvmMain at %p\n", (void*)(Address) uvmMain);
	UVMStack* mainStack = (UVMStack*) allocStack(STACK_SIZE, uvmMain, NULL);

	// init stack
	Address stackStart = mainStack->lowerBound;
	memset((void*) stackStart, 0, STACK_SIZE);
	mainStack->_sp = mainStack->_sp - 120;		// see java part: X64MachineCodeExpansion
												// the thread trampoline will pop registers

	inspectStack(mainStack, 50);

	// launch thread
	UVMThread* t = (UVMThread*) newThread((Address)mainStack);

	// join
	pthread_join(t->_pthread, NULL);

	return retval;
}
