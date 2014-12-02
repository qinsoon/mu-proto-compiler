#include "runtime.h"

void initMisc();

void initRuntime() {
    initHeap();
    initThread();
    initMisc();
}

static void handler(int sig, siginfo_t *si, void* unused) {
    printf("yieldpoint enabled\n");
//    disableYieldpoint();
    uVM_fail("havent implement yieldpoint");
}

void initMisc() {
    
    yieldpoint_protect_page = (Address) mmap(NULL, BYTES_IN_PAGE * 2, PROT_NONE, MAP_SHARED|MAP_ANON, -1, 0);
    yieldpoint_protect_page = alignUp(yieldpoint_protect_page, BYTES_IN_PAGE);
    DEBUG_PRINT(2, ("yieldpoint_protect_page=%llx\n", yieldpoint_protect_page));
    disableYieldpoint();
    
    // set up signal handler
    struct sigaction sa;
    sa.sa_flags = SA_SIGINFO;
    sigemptyset(&sa.sa_mask);
    sa.sa_sigaction = handler;
    if (sigaction(SIGBUS, &sa, NULL) == -1) {
        uVM_fail("Error when register signal handler");
    }
}

void disableYieldpoint() {
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_WRITE);
}

void enableYieldpoint() {
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_NONE);
}

Address alignUp(Address region, int align) {
    return (region + align - 1) & ~ (align - 1);
}

void fillAlignmentGap(Address start, Address end) {
    memset((void*)start, ALIGNMENT_VALUE, end - start);
}

void uVM_fail(const char* str) {
    printf("uVM failed in runtime: %s\n", str);
    exit(1);
}