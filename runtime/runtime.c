#include "runtime.h"

/*
 * global variables
 */

int64_t yieldpoint_check;
Address yieldpoint_protect_page;

Address heapStart;

ImmixSpace* immixSpace;
FreeListSpace* largeObjectSpace;

GCPhase_t phase;

void initYieldpoint();
void initSignalHandler();
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

void yieldpoint() {
    UVMThread* t = getThreadContext();
    
    DEBUG_PRINT(3, ("Thread%p reaches a yieldpoint\n", t->_pthread));
    
    // if we need to block
    if (t->_block_status == NEED_TO_BLOCK) {
    	// save rsp first
    	void* rsp;
    	void* rbp;

    	__asm__(
    			"mov %%rsp, %0		\n"
    			"mov %%rbp, %1		\n"
    			: "=rm" (rsp),
				  "=rm" (rbp)
		);
    	t->stack->_sp = (Address) rsp;
    	t->stack->_bp = (Address) rbp;

        block(t);
    }
}

//static void handler(int sig, siginfo_t *si, void* unused) {
//    printf("yieldpoint enabled\n");
////    disableYieldpoint();
//    uVM_fail("havent implement yieldpoint");
//}

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

void turnOffYieldpoints() {
    // checking
#ifdef CHECKING_YIELDPOINT
    yieldpoint_check = 0;
#endif
    
    // page protection
#ifdef PAGE_PROTECTION_YIELDPOINT
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_WRITE);
#endif
}

void turnOnYieldpoints() {
    // checking
#ifdef CHECKING_YIELDPOINT
    yieldpoint_check = 1;
#endif
    
    // page protection
#ifdef PAGE_PROTECTION_YIELDPOINT
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_NONE);
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

Address alignUp(Address region, int align) {
	if (align != 2 && align != 4 && align != 8 && align % 8 != 0) {
		printf("alignUp(), align=%d\n", align);
		uVM_fail("possibly wrong align in alignUp()");
	}

    return (region + align - 1) & ~ (align - 1);
}

void fillAlignmentGap(Address start, Address end) {
	if (end <= start)
		return;

    memset((void*)start, ALIGNMENT_VALUE, end - start);
}

void fillTypeInfo(TypeInfo* t, int64_t id, int64_t size, int64_t align,
		int64_t eleSize, int64_t length,
		int64_t nFixedRefOffsets, int64_t nFixedIRefOffsets) {
	t->id 				= id;
	t->size 			= size;
	t->align 			= align;
	t->eleSize 			= eleSize;
	t->length 			= length;
	t->nFixedRefOffsets = nFixedRefOffsets;
	t->nFixedIRefOffsets= nFixedIRefOffsets;
}

TypeInfo* allocScalarTypeInfo(int64_t id, int64_t size, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets) {
	TypeInfo* ret = (TypeInfo*) malloc(sizeof(TypeInfo) + (nRefOffsets + nIRefOffsets) * sizeof(int64_t));
	fillTypeInfo(ret, id, size, align, size, 1, nRefOffsets, nIRefOffsets);
	return ret;
}

TypeInfo* allocArrayTypeInfo (int64_t id, int64_t eleSize, int64_t length, int64_t align, int64_t nRefOffsets, int64_t nIRefOffsets) {
	TypeInfo* ret = (TypeInfo*) malloc(sizeof(TypeInfo) + (nRefOffsets + nIRefOffsets) * sizeof(int64_t));
	fillTypeInfo(ret, id, eleSize * length, align, eleSize, length, nRefOffsets, nIRefOffsets);
	return ret;
}

//TypeInfo* allocHybridTypeInfo(int64_t id, int64_t size, int64_t align, int64_t eleSize, int64_t length, int64_t nFixedRefOffsets, int64_t nVarRefOffsets) {
//	uVM_fail("allocHybridTypeInfo() unimplemented");
//	return NULL;
//}

int getTypeID(Address ref) {
	uint64_t header = * ((uint64_t*)ref);
//	printf("header = %llx\n", header);
	int id = (int) (header & 0x0000FFFF);
//	printf("id = %d\n", id);
	return id;
}

extern TypeInfo* typeInfoTable[];
extern int typeCount;
TypeInfo* getTypeInfo(Address ref) {
	int id = getTypeID(ref);
	uVM_assert(id >= 0 && id < typeCount, "invalid type id in getTypeInfo()");
	return typeInfoTable[id];
}

void printObject(Address ref) {
	TypeInfo* tinfo = getTypeInfo(ref);

	if (tinfo != NULL) {
		int64_t size = tinfo->size;

		printf("HEADER\n");
		printf("Address 0x%llx\t| 0x%llx\n", ref, *((Address*)ref));
		printf("OBJ\n");
		Address cur = ref + OBJECT_HEADER_SIZE;
		for (; cur < ref + OBJECT_HEADER_SIZE + size; cur += WORD_SIZE) {
			printf("Address 0x%llx\t| 0x%llx\n", cur, *((Address*)cur));
		}
	}
}

/*
 * bit map
 */
void set_bit(Word *words, int n) {
    words[WORD_OFFSET(n)] |= (1 << BIT_OFFSET(n));
}

void clear_bit(Word *words, int n) {
    words[WORD_OFFSET(n)] &= ~(1 << BIT_OFFSET(n));
}

int get_bit(Word *words, int n) {
    Word bit = words[WORD_OFFSET(n)] & (1 << BIT_OFFSET(n));
    return bit != 0;
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
