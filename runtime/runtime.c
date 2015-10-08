#include "runtime.h"
#include <stdio.h>
#include <signal.h>
#include <stdlib.h>


/*
 * global variables
 */
void initYieldpoint() {

#ifdef CHECKING_YIELDPOINT
    turnOffYieldpoints();
#endif


#ifdef PAGE_PROTECTION_YIELDPOINT
    yieldpoint_protect_page = (Address) mmap(NULL, BYTES_IN_PAGE * 2, PROT_NONE, MAP_SHARED|MAP_ANON, -1, 0);
    yieldpoint_protect_page = alignUp(yieldpoint_protect_page, BYTES_IN_PAGE);
    DEBUG_PRINT(3, ("yieldpoint_protect_page=%llx\n", yieldpoint_protect_page));
    disableYieldpoint();

    // set up signal handler
    struct sigaction sa;
    sa.sa_flags = SA_SIGINFO;
    sigemptyset(&sa.sa_mask);
    sa.sa_sigaction = handler;
    if (sigaction(SIGBUS, &sa, NULL) == -1) {
        uVM_fail("Error when register signal handler");
    }
#endif

}

void runtimeSignalHandler(int signum, siginfo_t *info, void *context) {
	void* si_addr = info->si_addr;
	int   si_code = info->si_code;

	char* sig;
	char* sig_code;
	switch(signum) {
	case SIGSEGV:
		sig = "SIGSEGV";
		switch (si_code) {
		case SEGV_MAPERR: sig_code = "Address not mapped (SEGV_MAPERR)"; break;
		case SEGV_ACCERR: sig_code = "Invalid permission (SEGV_ACCERR)"; break;
		}
		break;
	case SIGBUS:
		sig = "SIGBUS";
		switch (si_code) {
		case BUS_ADRALN: sig_code = "Invalid address alignment (BUS_ADRALN)"; break;
		case BUS_ADRERR: sig_code = "Non-existent physical address (BUS_ADRERR)"; break;
		case BUS_OBJERR: sig_code = "Object-specific hardware error (BUS_OBJERR)"; break;
		}
		break;
	}

	printf("VM abort for receiving signals\n");
	printf("  signal: %s\n", sig);
	printf("  sig code: %s\n", sig_code);
	printf("  address: %p\n", si_addr);
	printf("\n");

	// dump registers
    ucontext_t *psContext = (ucontext_t*)context;
    printf("--------------------------------------------\n");
    printf("Register RAX:   0x%llx\n", IA32_RAX(psContext));
    printf("Register RBX:   0x%llx\n", IA32_RBX(psContext));
    printf("Register RCX:   0x%llx\n", IA32_RCX(psContext));
    printf("Register RDX:   0x%llx\n", IA32_RDX(psContext));
    printf("Register RDI:   0x%llx\n", IA32_RDI(psContext));
    printf("Register RSI:   0x%llx\n", IA32_RSI(psContext));
    printf("Register RSP:   0x%llx\n", IA32_RSP(psContext));
    printf("Register RBP:   0x%llx\n", IA32_RBP(psContext));
    printf("Register RIP:   0x%llx\n", IA32_RIP(psContext));
//   printf("Register EFLAGS:0x%llx\n", IA32_RFLAGS(psContext));
//   printf("Register SS:   %llx\n",  IA32_SS(psContext));
//   printf("Register CS:  %llx\n", IA32_CS(psContext));
//   printf("Register DS:  %llx\n", IA32_DS(psContext));
//   printf("Register ES:  %llx\n", IA32_ES(psContext));
//   printf("Register FS:  %llx\n", IA32_FS(psContext));
//   printf("Register GS:  %llx\n", IA32_GS(psContext));
    printf("--------------------------------------------\n");

    // dump thread info
    for (int i = 0; i < threadCount; i++) {
    	UVMThread* t = uvmThreads[i];
    	if (t != NULL)
    		printThreadInfo(t);
    }
    // dump stack info
    for (int i = 0; i < stackCount; i++) {
    	UVMStack* s = uvmStacks[i];
    	if (s != NULL)
    		printStackInfo(s);
    }

	// trying to find out where the address belongs to
	Address addr = (Address) si_addr;
	bool found = false;

	// check if it is stack address
	for (int i = 0; i < stackCount; i++) {
		UVMStack* stack = uvmStacks[i];

		if (stack != NULL) {
			if (addr >= stack->lowerBound && addr <= stack->upperBound) {
				printf("The address is within stack %d\n", i);
				printStackInfo(stack);
				found = true;
			}

			if (addr >= stack->overflowGuard && addr <= stack->overflowGuard + BYTES_IN_PAGE) {
				printf("Stack overflow\n");
				printStackInfo(stack);
				found = true;
			}

			if (addr >= stack->underflowGuard && addr <= stack->underflowGuard + BYTES_IN_PAGE) {
				printf("Stack underflow\n");
				printStackInfo(stack);
				found = true;
			}
		}
	}

	// check if it is in immix space
	if (isInImmixSpace(addr)) {
		printf("The address is within immix space (from 0x%llx to 0x%llx)\n", immixSpace->immixStart, immixSpace->freelistStart);
		found = true;
	}

	// check if it is in large object space
	if (isInLargeObjectSpace(addr)) {
		printf("The address is within large object space\n");
		found = true;
	}

	if (!found)
		printf("The address is beyond VM/runtime-used address space. \n");

	uVM_fail("Caught signal (fatal error)");
}

struct sigaction sig_act;

void initSignalHandler() {
	memset(&sig_act, 0, sizeof(sig_act));

	sig_act.sa_sigaction = runtimeSignalHandler;
	sig_act.sa_flags = SA_SIGINFO;
	sigaction(SIGSEGV, &sig_act, NULL);
	sigaction(SIGBUS,  &sig_act, NULL);
}

/*
 * printing
 */

void uvmPrintPtr(int64_t s) {
	printf("0x%p\n", (void*)s);
}

void uvmPrintInt64(int64_t s) {
	printf("%lld\n", s);
}

void uvmPrintInt64ln(int64_t s) {
	printf("%lld\n", s);
}

void uvmPrintDouble(double s) {
	printf("inPrintDouble(), not working properly\n");
}

void uvmPrintStr(Address s) {
	printf("%s\n", (char*) s);
}

void uvmPrintStrln(Address s) {
	printf("%s\n", (char*) s);
}

void NOT_REACHED() {
	uVM_fail("SHOULDNT REACH HERE");
}

void uVM_fail(const char* str) {
    printf("uVM failed in runtime: %s\n", str);
    exit(1);
}

void uVM_suspend(const char* str) {
	printf("%s\n", str);
	getchar();
}
