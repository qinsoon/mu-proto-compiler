#include "runtime.h"
#include <stdlib.h>
#include <stdio.h>
#include <sys/mman.h>

UVMStack* uvmStacks[MAX_STACK_COUNT];
int stackCount = 0;
pthread_mutex_t stackAcctLock;

UnwindTable** unwindTable;
int unwindTableCount;

void initStack() {
	int i = 0;
	for (; i < MAX_STACK_COUNT; i++)
		uvmStacks[i] = NULL;

	pthread_mutex_init(&stackAcctLock, NULL);
}

UVMStack* getCurrentStack() {
	UVMThread* t = getThreadContext();
	return (UVMStack*) t->stack;
}

void addNewStack(UVMStack* stack) {
	pthread_mutex_lock(&stackAcctLock);

    uvmStacks[stackCount] = stack;
    stack->stackSlot = stackCount;
    stackCount++;

    pthread_mutex_unlock(&stackAcctLock);
}

void printStackInfo(UVMStack* s) {
	printf("----STACK INFO (%p)----\n", s);
    printf("slot=%lld\n", s->stackSlot);
    printf("range\n");
    printf("      =0x%llx ~ 0x%llx\n", s->lowerBound, s->upperBound);
    printf("overflow guard\n");
    printf("      =0x%llx ~ 0x%llx\n", s->overflowGuard, s->overflowGuard + BYTES_IN_PAGE - 1);
    printf("underflow guard\n");
    printf("      =0x%llx ~ 0x%llx\n", s->underflowGuard, s->underflowGuard + BYTES_IN_PAGE - 1);
    printf("   sp=0x%llx\n", s->_sp);
    printf("   bp=0x%llx\n", s->_bp);
    printf("   ip=0x%llx\n", s->_ip);
    printf("stackSize=%lld\n", s->stackSize);
    printf("entryFunc=%p\n", (void*)(Address) s->entry_func);
    printf("------------------\n");
}

void inspectStack(UVMStack* stack, int64_t maxValues) {
	Address stackTop = stack->upperBound;
	Address cur;

	printf("STACK STARTS (hi to lo, printing %lld values)\n", maxValues);
	printf("     -------------------\n");
	for (cur = stackTop - WORD_SIZE; cur >= stack->lowerBound && stackTop - cur < maxValues*WORD_SIZE; cur -= WORD_SIZE) {
		printf("     0x%llx | %llx \n", cur, *((uint64_t*)cur));
	}
	printf("     ...\n");
	printf("     -------------------\n");
}

void inspectStackBasedOnRBP(UVMStack* stack, Address rbp, int64_t frames) {
	printStackInfo(stack);

	for (int i = 0; i < frames; i++) {
		Address oldRBP = rbp;
		rbp = * ((Address*) rbp);
		Address cur;
		printf("#%d Frame----\n", i);
		for (cur = oldRBP + 2*WORD_SIZE; cur <= rbp + WORD_SIZE; cur += WORD_SIZE) {
			printf("     0x%llx | %llx ", cur, *((uint64_t*)cur));
			if (cur == rbp - WORD_SIZE)
				printf(" <-- FUNC ID\n");
			else if (cur == rbp)
				printf(" <-- RBP\n");
			else if (cur == rbp + WORD_SIZE)
				printf(" <-- RIP\n");
			else printf("\n");
		}
	}
}

extern int typeCount;

#define VERBOSE_STACK_SCAN false

void scanStackForRoots(UVMStack* stack, AddressNode** roots) {
	Address sp = stack->_sp;
	Address stackTop = stack->upperBound;
	Address curBP = stack->_bp;

	if (stackTop < sp)
		uVM_fail("sp is not under stack top");

	Address cur;

	int scannedSlots = 0;

	int frames = 0;

	int refs 	= 0;
	int irefs 	= 0;
	int nonRefs = 0;

	for (cur = sp; cur < stackTop; cur += WORD_SIZE) {
		scannedSlots ++;
		uint64_t candidate = *((uint64_t*)cur);

		if (cur == curBP) {
			if (VERBOSE_STACK_SCAN)
				printf("---finish a frame---\n");
			curBP = candidate;
			frames ++;
		}

		// check if *cur is a ref
		if (VERBOSE_STACK_SCAN)
			printf("checking value: 0x%llx at stack %p\n", candidate, (void*) cur);

		Address baseRef = findBaseRef(candidate);
		if (baseRef != (Address) NULL) {
			if (VERBOSE_STACK_SCAN) {
				printf("  baseref = %p\n", (void*) baseRef);
				if (isInImmixSpace(baseRef)) {
					printf("  in immix space\n");
				} else if (isInLargeObjectSpace(baseRef)){
					printf("  in large object space\n");
				} else {
					printf("  *** not in any space???\n");
				}
			}
			pushToList(baseRef, roots);

			if (baseRef == candidate)
				refs ++;
			else irefs ++;
		} else {
			nonRefs ++;
		}
	}

	if (VERBOSE_STACK_SCAN) {
	printf("Total scanned stack slots: %d, in %d frames\n", scannedSlots, frames);
	printf("   refs: %d\n", refs);
	printf("   irefs: %d\n", irefs);
	printf("   not objects: %d\n", nonRefs);
	printf("----------------\n");
	}
}

Address allocStack(int64_t stackSize, void*(*entry_func)(void*), void* args) {
    DEBUG_PRINT(3, ("Allocate for new stack (size:%lld, entry:%p, args:%p)\n", stackSize, (void*) (Address) entry_func, args));

    Address stackMeta = (Address) malloc(sizeof(UVMStack));

    if (stackMeta == 0)
        uVM_fail("error when trying to malloc space for uvmstack meta\n");

    Address stackAddr;
    int totalSizeForStack = BYTES_IN_PAGE * 2 + stackSize;		// two guard pages + acutal stack
    int retval = posix_memalign((void*)&stackAddr, BYTES_IN_PAGE, totalSizeForStack);

    if (retval != 0) {
    	printf("trying to alloc for stack (align:%d, size:%d)\n", BYTES_IN_PAGE, totalSizeForStack);
    	uVM_fail("error when trying to memalign alloc space for uvm stack\n");
    }

    // create stack and initialize
    UVMStack* stackStruct = (UVMStack*) stackMeta;
    stackStruct->_sp = stackAddr + BYTES_IN_PAGE + stackSize;
    stackStruct->_bp = stackStruct->_sp;
    stackStruct->_ip = 0;
    stackStruct->stackSize = stackSize;
    stackStruct->entry_func = entry_func;

    stackStruct->lowerBound = stackAddr + BYTES_IN_PAGE;
    stackStruct->overflowGuard = stackAddr;
    stackStruct->upperBound = stackStruct->_sp;
    stackStruct->underflowGuard = stackStruct->_sp;

    // protect over/underflow guard page
    int mprotectRet1 = mprotect((void*) stackStruct->overflowGuard, BYTES_IN_PAGE, PROT_NONE);
    if (mprotectRet1 != 0) {
    	printf("failed to mprotect stack overflow page: %llx\n", stackStruct->overflowGuard);
    	uVM_fail("failed to mprotect stack overflow page");
    }
    int mprotectRet2 = mprotect((void*) stackStruct->underflowGuard, BYTES_IN_PAGE, PROT_NONE);
    if (mprotectRet2 != 0) {
    	printf("failed to mprotect stack underflow page: %llx\n", stackStruct->overflowGuard);
    	uVM_fail("failed to mprotect stack underflow page");
    }

    // inform the runtime about the new stack
    addNewStack(stackStruct);

    printStackInfo(stackStruct);

    return stackMeta;
}

#define TRACE_EXCEPTION true

void throwException(Address exceptionObj) {
	if (TRACE_EXCEPTION)
		printf("throwing excpetion: 0x%llx\n", exceptionObj);

	// save exception object
	getCurrentStack()->exceptionObj = exceptionObj;

	// caller saved
	Address rax = 0, rcx = 0, rdx = 0, rdi = 0, rsi = 0;
	Address r8 = 0, r9 = 0, r10 = 0, r11 = 0;
//	Address xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7;
//	Address xmm8, xmm9, xmm10, xmm11, xmm12, xmm13, xmm14, xmm15;
	// callee saved
	Address rbx = 0, rbp = 0, r12 = 0, r13 = 0, r14 = 0;

	Address rsp = 0, rip = 0;

	// get current rsp, rbp (this C func frame)
	__asm__(
			"mov %%rsp, %0		\n"
			"mov %%rbp, %1		\n"
			: "=rm" (rsp),
			  "=rm" (rbp)
	);

	if (TRACE_EXCEPTION)
		inspectStackBasedOnRBP(getCurrentStack(), rbp, 2);

	if (TRACE_EXCEPTION)
		printf("RSP = 0x%llx, RBP = 0x%llx\n", rsp, rbp);

	Address ripFromGCC = (Address) __builtin_return_address(0);
	Address ripFromRBP = * ((Address*) (rbp + 8));
	uVM_assert(ripFromGCC == ripFromRBP, "rip not correct");

	// skip first frame (uvm wont catch exception in this one)
	rbp = * ((Address*)rbp);
	int64_t funcID = (int64_t) *((Word*)(rbp - 8));
	int64_t throwingFuncID = funcID;
	if (TRACE_EXCEPTION) {
		printf("This is the function that throws the exception, skip it\n");
		printf("RBP    = 0x%llx\n", rbp);
		printf("FuncID = %lld\n", throwingFuncID);
	}

	while (1) {
		rip = * ((Address*) (rbp + 8));

		// check rip to see if there is a valid frame below current frame
		// TODO: should use a sentinel value here
		if (rip == 0) {
			printf("Exception %llx is thrown from %lld, but no landingpad for it\n", exceptionObj, throwingFuncID);
			uVM_fail("failed to throw exception");
		}

		rbp = * ((Address*)rbp);
		int64_t funcID = (int64_t) *((Word*)(rbp - 8));

		if (TRACE_EXCEPTION) {
			printf("------\n");
			printf("RBP    = 0x%llx\n", rbp);
			printf("FuncID = %lld\n", funcID);
			printf("RIP    = 0x%llx\n", rip);
		}

		UnwindTable* table = unwindTable[funcID];

		// callee saved
		if (table->calleeSavedRegs.rbx != -1)
			rbx = rbp + table->calleeSavedRegs.rbx;
		if (table->calleeSavedRegs.r12 != -1)
			r12 = rbp + table->calleeSavedRegs.r12;
		if (table->calleeSavedRegs.r13 != -1)
			r13 = rbp + table->calleeSavedRegs.r13;
		if (table->calleeSavedRegs.r14 != -1)
			r14 = rbp + table->calleeSavedRegs.r14;

		for (int i = 0; i < table->callsitesN; i++) {
			if (TRACE_EXCEPTION)
				printf("check callsite %d, return address = 0x%llx, landing pad = 0x%llx\n",
					i, table->callsites[i].returnAddress, table->callsites[i].landingPad);

			if (rip == table->callsites[i].returnAddress) {
				// found call site, check if there is a landingpad
				if (TRACE_EXCEPTION)
					printf("found call site. \n");
				if (table->callsites[i].landingPad != 0) {
					// we are gonna unwind to this frame, and jump to the landing pad
					if (TRACE_EXCEPTION)
						printf("found landing pad, gonna jump there\n");

					// get caller-saved registers
					if (table->callsites[i].callerSavedRegs.rax != -1) {
						rax = rbp + table->callsites[i].callerSavedRegs.rax;
						if (TRACE_EXCEPTION)
							printf("getting rax from %llx\n", rax);
					}
					if (table->callsites[i].callerSavedRegs.rcx != -1) {
						rcx = rbp + table->callsites[i].callerSavedRegs.rcx;
						if (TRACE_EXCEPTION)
							printf("getting rcx from %llx\n", rcx);
					}
					if (table->callsites[i].callerSavedRegs.rdx != -1) {
						rdx = rbp + table->callsites[i].callerSavedRegs.rdx;
						if (TRACE_EXCEPTION)
							printf("getting rdx from %llx\n", rdx);
					}
					if (table->callsites[i].callerSavedRegs.rdi != -1) {
						rdi = rbp + table->callsites[i].callerSavedRegs.rdi;
						if (TRACE_EXCEPTION)
							printf("getting rdi from %llx\n", rdi);
					}
					if (table->callsites[i].callerSavedRegs.rsi != -1) {
						rsi = rbp + table->callsites[i].callerSavedRegs.rsi;
						if (TRACE_EXCEPTION)
							printf("getting rsi from %llx\n", rsi);
					}
					if (table->callsites[i].callerSavedRegs.r8 != -1) {
						r8 = rbp + table->callsites[i].callerSavedRegs.r8;
						if (TRACE_EXCEPTION)
							printf("getting r8 from %llx\n", r8);
					}
					if (table->callsites[i].callerSavedRegs.r9 != -1) {
						r9 = rbp + table->callsites[i].callerSavedRegs.r9;
						if (TRACE_EXCEPTION)
							printf("getting r9 from %llx\n", r9);
					}
					if (table->callsites[i].callerSavedRegs.r10 != -1) {
						r10 = rbp + table->callsites[i].callerSavedRegs.r10;
						if (TRACE_EXCEPTION)
							printf("getting r10 from %llx\n", r10);
					}
					if (table->callsites[i].callerSavedRegs.r11 != -1) {
						r11 = rbp + table->callsites[i].callerSavedRegs.r11;
						if (TRACE_EXCEPTION)
							printf("getting r11 from %llx\n", r11);
					}

					// rip
					rip = table->callsites[i].landingPad;

					if (TRACE_EXCEPTION)
						printf("Loading Callee-saved Registers: \n");
					Word vRBX = 0, vR12 = 0, vR13 = 0, vR14 = 0, vR15 = 0;

					if (TRACE_EXCEPTION)
						printf("Loading RBX from %llx", rbx);
					if (rbx != 0)
						vRBX = *((Word*) rbx);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRBX);

					if (TRACE_EXCEPTION)
						printf("Loading R12 from %llx", r12);
					if (r12 != 0)
						vR12 = *((Word*) r12);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR12);

					if (TRACE_EXCEPTION)
						printf("Loading R13 from %llx", r13);
					if (r13 != 0)
						vR13 = *((Word*) r13);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR13);

					if (TRACE_EXCEPTION)
						printf("Loading R14 from %llx", r14);
					if (r14 != 0)
						vR14 = *((Word*) r14);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR14);

//					printf("Loading R15 from %llx", r15);
//					if (r15 != 0)
//						vR15 = *((Word*) r15);
//					printf(" = %llx\n", vR15);

					if (TRACE_EXCEPTION)
						printf("Loading Caller-saved Registers: \n");
					Word vRAX = 0, vRCX = 0, vRDX = 0, vRDI = 0, vRSI = 0, vR8 = 0, vR9 = 0, vR10 = 0, vR11 = 0;

					if (TRACE_EXCEPTION)
						printf("Loading RAX from %llx", rax);
					if (rax != 0)
						vRAX = *((Word*) rax);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRAX);

					if (TRACE_EXCEPTION)
						printf("Loading RCX from %llx", rcx);
					if (rcx != 0)
						vRCX = *((Word*) rcx);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRCX);

					if (TRACE_EXCEPTION)
						printf("Loading RDX from %llx", rdx);
					if (rdx != 0)
						vRDX = *((Word*) rdx);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRDX);

					if (TRACE_EXCEPTION)
						printf("Loading RDI from %llx", rdi);
					if (rdi != 0)
						vRDI = *((Word*) rdi);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRDI);

					if (TRACE_EXCEPTION)
						printf("Loading RSI from %llx", rsi);
					if (rsi != 0)
						vRSI = *((Word*) rsi);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vRSI);

					if (TRACE_EXCEPTION)
						printf("Loading R8 from %llx", r8);
					if (r8 != 0)
						vR8  = *((Word*) r8);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR8);

					if (TRACE_EXCEPTION)
						printf("Loading R9 from %llx", r9);
					if (r9 != 0)
						vR9  = *((Word*) r9);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR9);

					if (TRACE_EXCEPTION)
						printf("Loading R10 from %llx", r10);
					if (r10 != 0)
						vR10 = *((Word*) r10);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR10);

					if (TRACE_EXCEPTION)
						printf("Loading R11 from %llx", r11);
					if (r11 != 0)
						vR11 = *((Word*) r11);
					if (TRACE_EXCEPTION)
						printf(" = %llx\n", vR11);

					Word vRBP = 0, vRSP = 0, vRIP = 0;
					if (TRACE_EXCEPTION)
						printf("Loading Special Registers:\n");

					if (TRACE_EXCEPTION)
						printf("RBP is %llx\n", rbp);
					if (rbp != 0)
						vRBP = rbp;
					if (TRACE_EXCEPTION)
						printf("Calculating RSP from (%llx + %d)\n", rbp, table->rspOffset);
					if (rsp != 0)
						vRSP = rbp + table->rspOffset;
					if (TRACE_EXCEPTION)
						printf("RIP is landingpad %llx\n", rip);
					if (rip != 0) {
						vRIP = rip;
					}

					// restore and jump to landingPad
					__asm__(
							// restore callee-saved register
							"movq	%0, %%rbx	\n"
							"movq	%1, %%r12	\n"
							"movq	%2, %%r13	\n"
							"movq	%3, %%r14	\n"
//							"movq	%4, %%r15	\n"
							// restore caller-saved register
							"movq	%5, %%rax	\n"
							"movq	%6, %%rcx	\n"
							"movq	%7, %%rdx	\n"
							"movq	%8, %%rdi	\n"
							"movq	%9, %%rsi	\n"
							"movq	%10, %%r8	\n"
							"movq	%11, %%r9	\n"
							"movq	%12, %%r10	\n"
							"movq	%13, %%r11	\n"
							// restore rsp
							"movq	%14, %%rsp	\n"
							// save rip (in r15, r15 is scratch register), then restore rbp (otherwise RBP is changed)
							"movq	%16, %%r15	\n"
							"movq	%15, %%rbp	\n"
							// jmp
							"jmp	*%%r15		\n"

							: // no output
							: "m" (vRBX),	// %0
							  "m" (vR12),	// %1
							  "m" (vR13),	// %2
							  "m" (vR14),	// %3
							  "m" (vR15),	// %4

							  "m" (vRAX),	// %5
							  "m" (vRCX),	// %6
							  "m" (vRDX),	// %7
							  "m" (vRDI),	// %8
							  "m" (vRSI),	// %9
							  "m" (vR8),	// %10
							  "m" (vR9),	// %11
							  "m" (vR10),	// %12
							  "m" (vR11),	// %13

							  "m" (vRSP),	// %14
							  "m" (vRBP),	// %15

							  "m" (vRIP)	// %16
							: // clobber
							  "rbx", "r12", "r13", "r14", "r15",
							  "rax", "rcx", "rdx", "rdi", "rsi", "r8", "r9", "r10", "r11",
							  "rbp", "rsp"
//							  "rip"
					);
				} else {
					if (TRACE_EXCEPTION)
						printf("no landing pad, keep unwinding\n");
				}
			}
		}
	}
}

Address landingPad() {
	Address ret = getCurrentStack()->exceptionObj;
	if (TRACE_EXCEPTION)
		printf("In landing pad, exceptionObj = 0x%llx\n", ret);
	return ret;
}

UnwindTable* allocateUnwindTable(int64_t callsites) {
	UnwindTable* ret = (UnwindTable*) malloc(sizeof(UnwindTable) + callsites * sizeof(X64CallsiteInfo));
	return ret;
}
