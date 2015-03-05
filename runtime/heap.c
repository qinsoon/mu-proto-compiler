#include "runtime.h"

// heap include an immix space (blocks) and a free list space.

void initHeap() {
    DEBUG_PRINT(5, ("initializing heap..\n"));
    
    heapStart = (Address) mmap(0, HEAP_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED | MAP_ANON, -1, 0);
    DEBUG_PRINT(5, ("-heap start at 0x%llx\n", heapStart));
    
    Address immixSpaceStart = alignUp(heapStart, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(5, ("-immix space start at 0x%llx (aligned to %d)\n", immixSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(heapStart, immixSpaceStart);
    
    Address endOfImmixSpace = immixSpaceStart + (HEAP_SIZE * HEAP_IMMIX_FRACTION);
    Address freelistSpaceStart = alignUp(endOfImmixSpace, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(5, ("-freelist space start at 0x%llx (aligned to %d)\n", freelistSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(endOfImmixSpace, freelistSpaceStart);
    
    immixSpace = newSpace(immixSpaceStart, freelistSpaceStart);
}

extern Address ImmixMutator_alloc(ImmixMutator* mutator, int64_t size, int64_t align);

Address allocObj(int64_t size, int64_t align) {
    DEBUG_PRINT(0, ("========Calling on allocObj========\n"));
                
    ImmixMutator* mutator = &(getThreadContext()->_mutator);
    DEBUG_PRINT(0, ("pthread=%llx, mutator=%llx\n", (Address)pthread_self(), (Address)mutator));
    
    return ImmixMutator_alloc(mutator, size, align);
}

/*
 * this method needs to cooperate with the object model 
 * implemented in uVM compiler (currently uvm.objectmodel.SimpleObjectModel)
 */
void initObj(Address addr, uint64_t header) {
    DEBUG_PRINT(0, ("========Calling on initObj========\n"));
    DEBUG_PRINT(0, ("addr=0x%llx, header=0x%llx\n", addr, header));

    *((uint64_t*) addr) = header;
}

void triggerGC() {
    // enable yieldpoint
    enableYieldpoint();
    
    // inform collector controller
    
    // make current thread wait
}