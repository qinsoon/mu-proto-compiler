#include "runtime.h"

/*
 * global variables
 */

int64_t yieldpoint_check;
Address yieldpoint_protect_page;

Address heapStart;

ImmixSpace* immixSpace;

GCPhase_t phase;

void initYieldpoint();

void initRuntime() {
    initHeap();
    initThread();
    initYieldpoint();
    initCollector();
    initStack();
}

void yieldpoint() {
    UVMThread* t = getThreadContext();
    
    DEBUG_PRINT(3, ("Thread%d reaches a yieldpoint\n", t->threadSlot));
    
    // if we need to block
    if (t->_block_status == NEED_TO_BLOCK) {
        block(t);
    }
}

static void handler(int sig, siginfo_t *si, void* unused) {
    printf("yieldpoint enabled\n");
//    disableYieldpoint();
    uVM_fail("havent implement yieldpoint");
}

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

Address alignUp(Address region, int align) {
    return (region + align - 1) & ~ (align - 1);
}

void fillAlignmentGap(Address start, Address end) {
    memset((void*)start, ALIGNMENT_VALUE, end - start);
}

void uvmPrintInt64(int64_t s) {
	printf("%lld\n", s);
}

void uvmPrintDouble(double s) {
	printf("inPrintDouble(), not working properly\n");
}

void uvmPrintStr(Address s) {
	printf("%s\n", (char*) s);
}

void NOT_REACHED() {
	uVM_fail("SHOULDNT REACH HERE");
}

void uVM_fail(const char* str) {
    printf("uVM failed in runtime: %s\n", str);
    exit(1);
}
