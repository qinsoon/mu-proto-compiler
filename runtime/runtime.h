#include "runtimetypes.h"
#include "debug.h"
#include "heap.h"
#include "threadstack.h"
#include "typeinfo.h"
#include "typeinfo.h"
#include "bitmap.h"

#include "osx_ucontext.h"

/*
 * FUNCTIONS
 */
extern void initRuntime();
extern void initThread();
extern void initHeap();
extern void initCollector();
extern void initStack();
