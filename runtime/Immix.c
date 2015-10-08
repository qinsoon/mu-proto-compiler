#include "runtime.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

/*
 * debug print struct
 */

void debug_printMutator(ImmixMutator* mutator) {
    printf("Mutator status (%llx): \n", (Address) mutator);
    printf(">>cursor=%llx, limit=%llx\n", mutator->cursor, mutator->limit);
    printf(">>line=%d\n", mutator->line);
    printf(">>blockStart=%llx, blockEnd=%llx\n", mutator->blockStart, mutator->blockEnd);
    printf(">>lineMark=%llx\n", (Address) mutator->lineMark);
}

void debug_printBlock(ImmixBlock* block) {
    printf("ImmixBlock: %llx (", block->start);
    if (block->next != NULL)
        printf("next: %llx ", block->next->start);
    if (block->prev != NULL)
        printf("prev: %llx", block->prev->start);
    printf(")\n");
    printf(">>state:%d, lineMarkTable:%llx\n", block->state, (Address)block->lineMarkTable);
}

ImmixSpace* newImmixSpace(Address immixStart, Address freelistStart) {
    ImmixSpace* ret = (ImmixSpace*) malloc(sizeof(ImmixSpace));
    
    ret->immixStart = immixStart;
    ret->freelistStart = freelistStart;
    
    size_t lineTableLength = (freelistStart - immixStart) / IMMIX_BYTES_IN_LINE;
    size_t lineTableSize = lineTableLength * sizeof(uint8_t);
    ret->lineMarkTable = (uint8_t*) malloc(lineTableSize);
    memset(ret->lineMarkTable, 0, lineTableSize);
    
    pthread_mutex_init(&(ret->lock), NULL);
    
    // initializing ImmixBlock
    Address cur = immixStart;
    uint8_t* curLineMark = ret->lineMarkTable;
    ImmixBlock* last = NULL;

    int totalBlocks = 0;

    for (; cur < freelistStart; cur += IMMIX_BYTES_IN_BLOCK, curLineMark = &(curLineMark[IMMIX_LINES_IN_BLOCK])) {
        ImmixBlock* b = (ImmixBlock*) malloc(sizeof(ImmixBlock));
        totalBlocks ++;
        
        if (last == NULL) {
            // first one
            ret->usableBlocks = b;
            b->prev = NULL;
        } else {
            last->next = b;
            b->prev = last;
        }
        
        b->state = IMMIX_BLOCK_MARK_USABLE;
        b->start = cur;
        b->lineMarkTable = curLineMark;
        
        last = b;
    }
    
    DEBUG_PRINT(3, ("Total blocks in Immix Space: %d\n", totalBlocks));

    return ret;
}

/*
 * Immix Mutator (Local)
 */

ImmixMutator* ImmixMutator_reset(ImmixMutator* mutator) {
    mutator->cursor = 0;
    mutator->limit = 0;
    
    mutator->lineMark = 0;
    
    mutator->blockStart = 0;
    mutator->blockEnd = 0;
    
    mutator->line = IMMIX_LINES_IN_BLOCK;
    
    return mutator;
}

ImmixMutator* ImmixMutator_init(ImmixMutator* mutator, ImmixSpace* space) {
    mutator->globalSpace = space;
    ImmixMutator_reset(mutator);
    return mutator;
}

// Immix Mutator
Address ImmixMutator_allocLarge(ImmixMutator*, int64_t, int64_t);
Address ImmixMutator_tryAllocFromLocal(ImmixMutator* mutator, int64_t size, int64_t align);

// Immix Space
int ImmixSpace_getNextAvailableLine(uint8_t* markTable, int currentLine);
int ImmixSpace_getNextUnavailableLine(uint8_t* markTable, int currentLine);
bool ImmixSpace_getNextBlock(ImmixMutator* mutator);

Address ImmixMutator_alloc(ImmixMutator* mutator, int64_t size, int64_t align) {
    DEBUG_PRINT(5, ("---alloc request---\n"));
    DEBUG_PRINT(5, ("size=%lld, align=%lld\n", size, align));
    
    // debug_printMutator(mutator);
    
    if (size > IMMIX_BYTES_IN_LINE) {
    	// thus no objects will span lines
        DEBUG_PRINT(5, ("Obj larger than a line, use allocLarge()\n"));
        return ImmixMutator_allocLarge(mutator, size, align);
    }
    
    Address start = alignUp(mutator->cursor, align);
    Address end = start + size;
    
    DEBUG_PRINT(5, ("after align: start=%llx, end=%llx\n", start, end));
    
    if (end > mutator->limit) {
        DEBUG_PRINT(5, ("current local lines are consumed, try get some from block\n"));
        Address ret = ImmixMutator_tryAllocFromLocal(mutator, size, align);
//        markInObjectMap(ret);	// we already marked it, since tryAllocFromLocal will call alloc()
        return ret;
    }
    
    fillAlignmentGap(mutator->cursor, start);
    mutator->cursor = end;
    
    DEBUG_PRINT(5, ("---alloc DONE: %llx---\n", start));
    markInObjectMap(start);
    return start;
}

Address ImmixMutator_tryAllocFromLocal(ImmixMutator* mutator, int64_t size, int64_t align) {
    DEBUG_PRINT(5, ("tryAllocFromLocal\n"));
    
    if (mutator->line < IMMIX_LINES_IN_BLOCK) {
        int nextAvailableLine = ImmixSpace_getNextAvailableLine(mutator->lineMark, mutator->line);
        DEBUG_PRINT(5, ("currentline: %d, nextAvailableLine: %d\n", mutator->line, nextAvailableLine));
        
        if (nextAvailableLine < IMMIX_LINES_IN_BLOCK) {
            DEBUG_PRINT(5, ("We will get more lines from current block\n"));
            // we still have lines in this block
            int endLine = ImmixSpace_getNextUnavailableLine(mutator->lineMark, nextAvailableLine);
            mutator->cursor = mutator->blockStart + (nextAvailableLine << IMMIX_LOG_BYTES_IN_LINE);
            mutator->limit  = mutator->blockStart + (endLine << IMMIX_LOG_BYTES_IN_LINE);
            memset((void*)mutator->cursor, 0, mutator->limit - mutator->cursor);
            mutator->line = endLine;
            
            // wet this new line as fresh_alloc
            for (int i = nextAvailableLine; i < endLine; i++)
            	mutator->lineMark[i] = IMMIX_LINE_MARK_FRESH_ALLOC;

            return ImmixMutator_alloc(mutator, size, align);
        }
    }

    // we run out of place for this block
    // require block from space
    while (true) {
        bool succ = ImmixSpace_getNextBlock(mutator);
        if (succ) {
            return ImmixMutator_alloc(mutator, size, align);
        }
    }
}

extern Address allocLarge(FreeListSpace* flSpace, int64_t size, int64_t align);

Address ImmixMutator_allocLarge(ImmixMutator* mutator, int64_t size, int64_t align) {
	return allocLarge(largeObjectSpace, size, align);
}

int ImmixSpace_getNextAvailableLine(uint8_t* markTable, int currentLine) {
    int i = currentLine;
    while (markTable[i] != IMMIX_LINE_MARK_FREE)
        i++;

    return i;
}

int ImmixSpace_getNextUnavailableLine(uint8_t* markTable, int currentLine) {
    int i = currentLine;
    while (i < IMMIX_LINES_IN_BLOCK && markTable[i] == IMMIX_LINE_MARK_FREE)
        i++;
    
    return i;
}

uint8_t* ImmixSpace_getLineMarkByte(ImmixSpace* space, Address obj) {
	return & (space->lineMarkTable[(obj - space->immixStart) / IMMIX_BYTES_IN_LINE]);
}

/*
 * Immix Space (Global, synchronization required)
 */

bool ImmixSpace_getNextBlock(ImmixMutator* mutator) {
    DEBUG_PRINT(4, ("acquiring from global memory (getNextBlock())\n"));
    ImmixSpace* space = mutator->globalSpace;
    
    // lock acquired
    DEBUG_PRINT(5, ("acquiring lock..."));
    pthread_mutex_lock( &(space->lock));
    DEBUG_PRINT(5, ("done\n"));
    
    // get a new block
    ImmixBlock* newBlock = space->usableBlocks;
    if (newBlock != NULL) {
        DEBUG_PRINT(5, ("got next block: %llx\n", newBlock->start));
        // debug_printBlock(newBlock);
        
        // success
        // remove from usableBlock
        space->usableBlocks = newBlock->next;
        if (newBlock->next != NULL)
            space->usableBlocks->prev = NULL;
        DEBUG_PRINT(5, ("removed from usableBlocks\n"));
        
        // add to usedBlock
        newBlock->prev = NULL;
        newBlock->next = space->usedBlocks;
        if (space->usedBlocks != NULL)
            space->usedBlocks->prev = newBlock;
        space->usedBlocks = newBlock;
        space->used = space->used + IMMIX_BYTES_IN_BLOCK;
        DEBUG_PRINT(5, ("adding to usedBlocks\n"));
        
        // set mutator with new block
        mutator->cursor = newBlock->start;
        mutator->limit  = newBlock->start;
        mutator->line   = 0;
        mutator->blockStart = newBlock->start;
        mutator->blockEnd = newBlock->start + IMMIX_BYTES_IN_BLOCK;
        mutator->lineMark = newBlock->lineMarkTable;
        
        DEBUG_PRINT(5, ("acquiring global succ\n"));
        pthread_mutex_unlock( &(space->lock));
        return true;
    } else {
        DEBUG_PRINT(5, ("acquiring global fail\n"));
        // return false to require a GC
        pthread_mutex_unlock( &(space->lock));
        
        // the thread will block here
        triggerGC();
        
        return false;
    }
}

/*
 * Immix Collector
 */
void ImmixCollector_markObject(ImmixSpace* space, Address objectRef) {
	// mark the header as traced/alive
	setMarkBitInHeader(objectRef, OBJECT_HEADER_MARK_BIT_MASK, markState);

	// mark the immix line as live
	uint8_t* lineMark = ImmixSpace_getLineMarkByte(space, objectRef);
	*lineMark = IMMIX_LINE_MARK_LIVE;
}

void ImmixCollector_release(ImmixSpace* space) {
	// walk through used blocks
	ImmixBlock* curBlock = space->usedBlocks;

	int freeLines = 0;
	int liveLines = 0;
	int usableBlocks = 0;
	int fullBlocks = 0;

	while (curBlock != NULL) {
		bool hasFreeLines = false;

		// walk through lines
		for (int i = 0; i < IMMIX_LINES_IN_BLOCK; i++) {
			if (curBlock->lineMarkTable[i] != IMMIX_LINE_MARK_LIVE) {
				hasFreeLines = true;
				curBlock->lineMarkTable[i] = IMMIX_LINE_MARK_FREE;

				freeLines ++;

				// zeroing the line
				Address lineStart = curBlock->start + i * IMMIX_BYTES_IN_LINE;
				memset((void*)lineStart, 0, IMMIX_BYTES_IN_LINE);

				// clear objectmap
				clearRangeInObjectMap(lineStart, IMMIX_BYTES_IN_LINE);
			} else {
				liveLines ++;
			}
		}

		if (hasFreeLines) {
			// this block is usable
			curBlock->state = IMMIX_BLOCK_MARK_USABLE;
			usableBlocks++;

			// save next block we want to iterate
			ImmixBlock* next = curBlock->next;

			// remove the block from space->usedBlocks
			if (curBlock->prev == NULL) {
				// this is the first block in space->usedBlocks
				space->usedBlocks = curBlock->next;
			} else {
				curBlock->prev->next = curBlock->next;
			}

			if (curBlock->next != NULL)
				curBlock->next->prev = curBlock->prev;

			// put the block to space->usableBlocks
			ImmixBlock* curFirstUsableBlock = space->usableBlocks;

			space->usableBlocks = curBlock;
			curBlock->next = curFirstUsableBlock;
			curBlock->prev = NULL;
			if (curFirstUsableBlock != NULL) {
				curFirstUsableBlock->prev = curBlock;
			}

			// iterate to next block
			curBlock = next;
		} else {
			// this block is full
			curBlock->state = IMMIX_BLOCK_MARK_FULL;
			fullBlocks ++;
		}
	}

	printf("free lines = %d\n", freeLines);
	printf("live lines = %d\n", liveLines);
	printf("usable blocks = %d\n", usableBlocks);
	printf("full blocks   = %d\n", fullBlocks);
}
