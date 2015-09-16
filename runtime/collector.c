#include "runtime.h"

pthread_t controller;

pthread_mutex_t controller_mutex;
pthread_cond_t controller_cond;

void *collector_controller_run(void *param);

void initCollector() {
    // controller
    int ret = pthread_create(&controller, NULL, collector_controller_run, NULL);
    if (ret) {
        uVM_fail("failed to create controller thread");
    }
}

void wakeCollectorController() {
    pthread_mutex_lock(&controller_mutex);
    phase = BLOCKING_FOR_GC;
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
				printf("Roots:\n");
				AddressNode* cur = roots;
				for (; cur != NULL; cur = cur->next) {
					printf("0x%llx\n", cur->addr);
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
	printf("tracing 0x%llx\n", ref);

	TypeInfo* typeInfo = getTypeInfo(ref);
	for (int i = 0; i < typeInfo->nFixedRefOffsets; i++) {
		Address field = ref + OBJECT_HEADER_SIZE + typeInfo->refOffsets[i];

		// field could be iref
		// need to find its base ref here
		Address fieldObj = findBaseRef(field);
		printf("field: 0x%llx\n", field);
		printf("fieldBase: 0x%llx\n", fieldObj);

		if (fieldObj == (Address) NULL)
			uVM_fail("cannot find a base ref for a ref field");

		pushToList(fieldObj, &alive);
	}
}

void traceObjects() {
	// trace roots
	while (roots != NULL) {
		AddressNode* node = popFromList(&roots);
		Address ref = node->addr;
		free(node);

		traceObject(ref);
	}

	while (alive != NULL) {
		AddressNode* node = popFromList(&alive);
		Address ref = node->addr;
		free(node);

		traceObject(ref);
	}
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
        phase = BLOCKED_FOR_GC;
        turnOffYieldpoints();
        
        // start to work
        DEBUG_PRINT(1, ("Collector is going to work (currently sleep for 1 secs)\n"));
        phase = GC;
//        usleep(1000000);
        scanGlobal();	// empty
        scanStacks();
        scanRegisters();

        traceObjects();
        
        uVM_fail("didnt implement sweeping");

        // reset all mutators
        for (int i = 0; i < threadCount; i++) {
            UVMThread* t = uvmThreads[i];
            if (t != NULL) {
                ImmixMutator_reset(&(t->_mutator));
            }
        }
        
        // unblock all the mutators
        DEBUG_PRINT(1, ("Collector Controller is going to unblock all mutators\n"));
        
        phase = MUTATOR;
        
        for (int i = 0; i < threadCount; i++) {
            UVMThread* t = uvmThreads[i];
            if (t != NULL) {
                unblock(t);
            }
        }
    }
}
