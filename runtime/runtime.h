#include "runtimetypes.h"
#include "debug.h"
#include "heap.h"
#include "threadstack.h"
#include "typeinfo.h"
#include "bitmap.h"

#include "osx_ucontext.h"

/*
 * FUNCTIONS
 */
extern void initRuntime();

extern void initHeap();
extern void initCollector();
extern void initThread();
extern void initStack();
extern void initYieldpoint();
extern void initSignalHandler();
