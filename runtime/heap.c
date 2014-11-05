#include "runtime.h"

// heap include an immix space (blocks) and a free list space.

void initHeap() {
    DEBUG_PRINT(("initializing heap..\n"));
    
    heapStart = (Address) mmap(0, HEAP_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED | MAP_ANON, -1, 0);
    DEBUG_PRINT(("-heap start at 0x%lx\n", heapStart));
    
    immixSpaceStart = align(heapStart, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(("-immix space start at 0x%lx (aligned to %d)\n", immixSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(heapStart, immixSpaceStart);
    
    Address endOfImmixSpace = immixSpaceStart + (HEAP_SIZE * HEAP_IMMIX_FRACTION);
    freelistSpaceStart = align(endOfImmixSpace, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(("-freelist space start at 0x%lx (aligned to %d)\n", freelistSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(endOfImmixSpace, freelistSpaceStart);
}