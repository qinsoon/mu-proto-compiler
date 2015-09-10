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

AddressNode* roots;
AddressNode* alive;

AddressNode* pushToList(Address addr, AddressNode** list) {
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
				scanStackForRoots(stack, roots);
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
        uVM_fail("didnt implement tracing");
        
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
