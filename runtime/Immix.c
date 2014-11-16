#include "runtime.h"

ImmixSpace* newSpace(Address immixStart, Address freelistStart) {
    ImmixSpace* ret = (ImmixSpace*) malloc(sizeof(ImmixSpace));
    
    ret->immixStart = immixStart;
    ret->freelistStart = freelistStart;
    
    return ret;
}

ImmixMutator* newMutator(ImmixSpace* space) {
    ImmixMutator* m = (ImmixMutator*) malloc(sizeof(ImmixMutator));
    m->globalSpace = space;
    return m;
}

/*
 * Immix Mutator
 */

ImmixMutator* ImmixMutator_reset(ImmixMutator* mutator) {
    mutator->cursor = 0;
    mutator->limit = 0;
    mutator->largeCursor = 0;
    mutator->largeLimit = 0;
    
    mutator->markTable = 0;
    mutator->recyclableBlock = 0;
    
    mutator->line = IMMIX_LINES_IN_BLOCK;
    mutator->lineUseCount = 0;
    
    mutator->recyclableExhausted = false;
    
    return mutator;
}

ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space) {
    mutator->globalSpace = space;
    ImmixMutator_reset(mutator);
    return mutator;
}


Address ImmixMutator_alloc(ImmixMutator* mutator, int64_t size, int64_t align) {
    DEBUG_PRINT(("---alloc request---\n"));
    DEBUG_PRINT(("size=%d, align=%d\n", size, align));
    
    if (size > IMMIX_BYTES_IN_LINE) {
        DEBUG_PRINT(("Obj larger than a line, use allocLarge()\n"));
        return ImmixMutator_allocLarge(mutator, size, align);
    }
    
    Address start = align(mutator->cursor, align);
    Address end = start + size;
    DEBUG_PRINT(("cursor=%lx, limit=%lx\n", mutator->cursor, mutator->limit));
    DEBUG_PRINT(("start=%lx, end=%lx\n", start, end));
    
    if (end > mutator->limit) {
        DEBUG_PRINT(("current local lines are consumed, try get some from block\n"));
        return ImmixMutator_tryAllocFromCurrentBlock(mutator, size, align);
    }
    
    fillAlignmentGap(mutator->cursor, start);
    mutator->cursor = end;
    
    return start;
}

Address ImmixMutator_tryAllocFromCurrentBlock(ImmixMutator* mutator, int64_t size, int64_t align) {
    
}