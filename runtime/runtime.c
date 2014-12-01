#include "runtime.h"

void initMisc();

void initRuntime() {
    initHeap();
    initThread();
    initMisc();
}

void initMisc() {
    yieldpoint_protect_page = (Address) malloc(BYTES_IN_PAGE);
    disableYieldpoint();
}

extern void disableYieldpoint() {
    mprotect((void*) yieldpoint_protect_page, BYTES_IN_PAGE, PROT_WRITE);
}

extern void enableYieldpoint() {
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