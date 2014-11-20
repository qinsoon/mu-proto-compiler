#include "runtime.h"

void initRuntime() {
    initHeap();
    initThread();
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