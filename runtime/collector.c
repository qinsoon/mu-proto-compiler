#include "runtime.h"

#include <stdlib.h>
#include <stdio.h>

pthread_t controller;

pthread_mutex_t controller_mutex;
pthread_cond_t controller_cond;

GCPhase_t phase;
pthread_mutex_t gcPhaseLock;

#define VERBOSE_GC false
#define VALIDATE_HEAP false

void *collector_controller_run(void *param);

void initCollector() {
	pthread_mutex_init(&gcPhaseLock, NULL);

    // controller
    int ret = pthread_create(&controller, NULL, collector_controller_run, NULL);
    if (ret) {
        uVM_fail("failed to create controller thread");
    }
}

void wakeCollectorController() {
    pthread_mutex_lock(&controller_mutex);

    pthread_mutex_lock(&gcPhaseLock);
    phase = BLOCKING_FOR_GC;
    pthread_mutex_unlock(&gcPhaseLock);

    pthread_cond_signal(&controller_cond);
    pthread_mutex_unlock(&controller_mutex);
}

// when passed for push and pop, always use these two with &
AddressNode* roots;
AddressNode* alive;

AddressNode* pushToList(Address addr, AddressNode** list) {
	// check if it already exists
	AddressNode* cur = *list;
	for (; cur != NULL; cur = cur->next) {
		if (cur->addr == addr)
			return cur;
	}

	AddressNode* ret = (AddressNode*) malloc(sizeof(AddressNode));
	ret->addr = addr;

	ret->next = *list;	// this is fine even if ret is the first elem in the list
	*list = ret;

	return ret;
}

AddressNode* popFromList(AddressNode** list) {
	AddressNode* ret = *list;

	if (ret != NULL)
		*list = ret->next;
	else *list = NULL;

	return ret;
}

void scanGlobal() {}

void scanStacks() {
	for (int i = 0; i < stackCount; i++) {
		UVMStack* stack = uvmStacks[i];
		if (stack != NULL) {
			if (stack->thread != NULL) {
				// scan it
				scanStackForRoots(stack, &roots);

				// check roots
				if (VERBOSE_GC) {
					printf("Roots:\n");
					AddressNode* cur = roots;
					for (; cur != NULL; cur = cur->next) {
						printf("0x%llx\n", cur->addr);
					}
				}
			} else {
				// this is an inactive stack, we didn't have correct rsp and register values on the stack yet?
				uVM_fail("cant scan an inactive stack");
			}
		}
	}
}

void scanRegisters() {
	// suppose all the registers that represent uvm values are on the stack already
	// do nothing here
}

void traceObject(Address ref) {
	TypeInfo* typeInfo = getTypeInfo(ref);

	uVM_assert(typeInfo != NULL, "meet a non-ref in traceObject()");

	if (VERBOSE_GC) {
		printf("tracing 0x%llx of type %lld\n", ref, typeInfo->id);
		printObject(ref);
	}

	// mark this object as alive/traced
	setMarkBitInHeader(ref, OBJECT_HEADER_MARK_BIT_MASK, markState);
	// additionally we need to mark lines for immix space
	if (isInImmixSpace(ref))
		ImmixSpace_markObject(immixSpace, ref);

	for (int i = 0; i < typeInfo->nFixedRefOffsets; i++) {
		Address field = ref + OBJECT_HEADER_SIZE + typeInfo->refOffsets[i];

		Address ref = * ((Address*) field);
		if (VERBOSE_GC) {
			printf("field: 0x%llx\n", field);
			printf("->ref: 0x%llx\n", ref);
		}

		if (ref != (Address) NULL && !testMarkBitInHeader(ref, OBJECT_HEADER_MARK_BIT_MASK, markState))
			pushToList(ref, &alive);
	}

	for (int i = typeInfo->nFixedRefOffsets; i < typeInfo->nFixedRefOffsets + typeInfo->nFixedIRefOffsets; i++) {
		Address field = ref + OBJECT_HEADER_SIZE + typeInfo->refOffsets[i];

		Address iref = *((Address*) field);
		Address ref  = findBaseRef(iref);
		if (VERBOSE_GC) {
			printf("field: 0x%llx\n ", field);
			printf("->iref:0x%llx\n", iref);
			printf("->ref: 0x%llx\n", ref);
		}

		if (ref != (Address) NULL && !testMarkBitInHeader(ref, OBJECT_HEADER_MARK_BIT_MASK, markState))
			pushToList(ref, &alive);
	}
}

void traceObjects() {
	// trace roots
	while (roots != NULL) {
		AddressNode* node = popFromList(&roots);
		Address ref = node->addr;
		free(node);
		Address baseRef = findBaseRef(ref);
		traceObject(baseRef);
	}

	while (alive != NULL) {
		AddressNode* node = popFromList(&alive);
		Address ref = node->addr;
		free(node);

		traceObject(ref);
	}
}

void validateObjectMap() {
	printf("==== valiedate object map ====\n");
    int i = 0;
    int objects = 0;
    for (; i < objectMap->bitmapSize; i++) {
    	if (get_bit(objectMap->bitmap, i) != 0) {
    		objects++;
        	Address obj = objectMapIndexToAddress(i);
        	printObject(obj);
    	}
    }
    printf("total objects in objectmap: %d\n", objects);
    printf("============================== \n");
}

void validateLargeObjectSpace() {
	printf("==== valiedate large object space ====\n");
    int objects = 0;

    FreeListNode* cur = largeObjectSpace->head;

    while (cur != NULL) {
    	printObject(cur->addr);
    	objects++;
    }

    printf("total objets in large object space: %d\n", objects);
    printf("======================================\n");
}

void validateHeap() {
	validateObjectMap();
	validateLargeObjectSpace();
}

void release() {
	ImmixSpace_release(immixSpace);
	FreeListSpace_release(largeObjectSpace);
}

void prepare() {
	ImmixSpace_prepare(immixSpace);
}

void *collector_controller_run(void *param) {
    DEBUG_PRINT(1, ("Collector Controller running...\n"));
    
    pthread_mutex_init(&controller_mutex, NULL);
    pthread_cond_init(&controller_cond, NULL);
    
    // main loop
    while (true) {
        // sleep
        DEBUG_PRINT(1, ("Collector Controller goes asleep\n"));
        pthread_mutex_lock(&controller_mutex);
        while (phase != BLOCKING_FOR_GC) {
            pthread_cond_wait(&controller_cond, &controller_mutex);
        }
        pthread_mutex_unlock(&controller_mutex);
        
        // block all the mutators
        DEBUG_PRINT(1, ("Collector Controller is awaken to block all mutators\n"));
        for (int i = 0; i < threadCount; i++) {
            UVMThread* t = uvmThreads[i];
            if (t != NULL && t->_block_status == RUNNING) {
                t->_block_status = NEED_TO_BLOCK;
            }
        }
        
        // ensure everything is blocked
        DEBUG_PRINT(1, ("Collector Controller is waiting for all mutators to block\n"));
        
        bool worldStopped = false;
        while (!worldStopped) {
            worldStopped = true;
            for (int i = 0; i < threadCount; i++) {
                UVMThread* t = uvmThreads[i];
//                DEBUG_PRINT(3, ("Checking on Thread%d...", t->threadSlot));
                if (t != NULL && t->_block_status == BLOCKED) {
//                	DEBUG_PRINT(3, ("blocked\n"));
                	continue;
                }
                else {
//                	DEBUG_PRINT(3, ("not blocked. Status: %d\n", t->_block_status));
                    worldStopped = false;
                    break;
                }
            }
        }
        
        DEBUG_PRINT(1, ("All mutators blocked\n"));

        pthread_mutex_lock(&gcPhaseLock);
        phase = BLOCKED_FOR_GC;
        pthread_mutex_unlock(&gcPhaseLock);

        turnOffYieldpoints();
        
        // start to work
        DEBUG_PRINT(1, ("Collector is going to work\n"));
        pthread_mutex_lock(&gcPhaseLock);
        phase = GC;
        pthread_mutex_unlock(&gcPhaseLock);

        if (VALIDATE_HEAP) {
        	validateObjectMap();
        }

        prepare();

        scanGlobal();	// empty
        scanStacks();
        scanRegisters();

        traceObjects();
        
        release();

        // reset all mutators
        for (int i = 0; i < threadCount; i++) {
            UVMThread* t = uvmThreads[i];
            if (t != NULL) {
                ImmixMutator_reset(&(t->_mutator));
            }
        }

        if (VALIDATE_HEAP) {
        	validateHeap();
        }

        if (VERBOSE_GC) {
        	printf("mark state = %llx \n", markState);
        }
        flipBit(OBJECT_HEADER_MARK_BIT_MASK, &markState);
        if (VERBOSE_GC) {
        	printf("mark state (after flip) = %llx\n", markState);
        }
//        uVM_suspend("check mark state");

        // unblock all the mutators
        DEBUG_PRINT(1, ("Collector Controller is going to unblock all mutators\n"));
        uVM_suspend("gc ends");
        
        pthread_mutex_lock(&gcPhaseLock);
        phase = MUTATOR;
        pthread_mutex_unlock(&gcPhaseLock);
        
        for (int i = 0; i < threadCount; i++) {
            UVMThread* t = uvmThreads[i];
            if (t != NULL) {
                unblock(t);
            }
        }
    }
}

void triggerGC() {
	pthread_mutex_lock(&gcPhaseLock);
	if (phase != MUTATOR) {
		pthread_mutex_unlock(&gcPhaseLock);
		yieldpoint();
		return;
	}
	pthread_mutex_unlock(&gcPhaseLock);

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
