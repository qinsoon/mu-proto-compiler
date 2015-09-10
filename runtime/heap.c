#include "runtime.h"

// heap include an immix space (blocks) and a free list space.

FreeListSpace* newFreeListSpace();

void initHeap() {
    DEBUG_PRINT(1, ("initializing heap..\n"));
    
    heapStart = (Address) mmap(0, HEAP_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED | MAP_ANON, -1, 0);
    DEBUG_PRINT(1, ("-heap start at 0x%llx\n", heapStart));
    
    Address immixSpaceStart = alignUp(heapStart, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(1, ("-immix space start at 0x%llx (aligned to %d)\n", immixSpaceStart, IMMIX_BYTES_IN_BLOCK));
    fillAlignmentGap(heapStart, immixSpaceStart);
    
    Address endOfImmixSpace = immixSpaceStart + (HEAP_SIZE * HEAP_IMMIX_FRACTION);
    Address freelistSpaceStart = alignUp(endOfImmixSpace, IMMIX_BYTES_IN_BLOCK);
    DEBUG_PRINT(1, ("-freelist space start at 0x%llx (aligned to %d)\n", freelistSpaceStart, IMMIX_BYTES_IN_BLOCK));	// not using this "freelist space"
    fillAlignmentGap(endOfImmixSpace, freelistSpaceStart);
    
    immixSpace = newImmixSpace(immixSpaceStart, freelistSpaceStart);
    largeObjectSpace = newFreeListSpace();
}

extern Address ImmixMutator_alloc(ImmixMutator* mutator, int64_t size, int64_t align);

Address allocObj(int64_t size, int64_t align) {
    DEBUG_PRINT(5, ("========Calling on allocObj========\n"));
                
    ImmixMutator* mutator = &(getThreadContext()->_mutator);
    DEBUG_PRINT(5, ("pthread=%llx, mutator=%llx\n", (Address)pthread_self(), (Address)mutator));
    
    return ImmixMutator_alloc(mutator, size, align);
}

/*
 * this method needs to cooperate with the object model 
 * implemented in uVM compiler (currently uvm.objectmodel.SimpleObjectModel)
 */
void initObj(Address addr, uint64_t header) {
    DEBUG_PRINT(5, ("========Calling on initObj========\n"));
    DEBUG_PRINT(5, ("addr=0x%llx, header=0x%llx\n", addr, header));

    *((uint64_t*) addr) = header;
}

void triggerGC() {
    // enable yieldpoint
    turnOnYieldpoints();
    
    // inform collector controller (it will ensure all threads are blocked)
    wakeCollectorController();
    
    // make current thread wait
    UVMThread* cur = getThreadContext();
    cur->_block_status = NEED_TO_BLOCK;
    yieldpoint();
    
    // the thread won't reach here until GC is done
}

void spaceInfo() {
	printf("Immix Space       : used = %lld bytes\n", immixSpace->used);
	printf("Large Object Space: used = %lld bytes\n", largeObjectSpace->used);
}

bool isInImmixSpace(Address addr) {
	// check Immix space first
	if (immixSpace->immixStart <= addr && immixSpace->freelistStart >= addr)
		return true;
	else return false;
}

bool isInLargeObjectSpace(Address addr) {
	FreeListNode* node = largeObjectSpace->head;
	FreeListNode* cur;
	for (cur = node; cur != NULL; cur = cur->next) {
		if (cur->addr <= addr && cur->addr + cur->size >= addr)
			return true;
	}
	return false;
}

/*
 * freelist space (now using global malloc)
 */
FreeListSpace* newFreeListSpace() {
	FreeListSpace* ret = (FreeListSpace*) malloc(sizeof(FreeListSpace));

	ret->head = NULL;
	ret->last = NULL;
	ret->used = 0;
	pthread_mutex_init(&(ret->lock), NULL);

	return ret;
}

Address allocLarge(FreeListSpace* flSpace, int64_t size, int64_t align) {
	DEBUG_PRINT(3, ("acquiring from global memory (allocLarge(), size=%lld, align=%lld)\n", size, align));
	pthread_mutex_lock( &(flSpace->lock));

	// actually allocation
//	Address addr;
//	int ret = posix_memalign((void*)&addr, align, size);
//	if (ret != 0) {
//		printf("trying to alloc from freelist space: size=%lld, align=%lld\n", size, align);
//		uVM_fail("failed posix_memalign alloc");
//	}
	Address addr = (Address) malloc(size);

	// metadata
	FreeListNode* node = (FreeListNode*) malloc(sizeof(FreeListNode));
	node->next = NULL;
	node->addr = addr;
	node->size = size;

	// update freelist space
	if (flSpace->last == NULL) {
		// first node
		flSpace->head = node;
		flSpace->last = node;
	} else {
		flSpace->last->next = node;
	}
	flSpace->used = flSpace->used + size;

	pthread_mutex_unlock( &(flSpace->lock));

	return addr;
}
