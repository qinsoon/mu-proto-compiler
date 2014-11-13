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
    return (Address) malloc(size);
}