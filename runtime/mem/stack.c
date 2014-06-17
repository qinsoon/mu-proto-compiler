#include <stdlib.h>

#define STACK_SIZE 10 * 1024 * 1024 // size in bytes

void* allocateStack() {
    return malloc(STACK_SIZE);
}