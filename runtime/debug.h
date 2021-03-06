#ifndef DEBUG_H
#define DEBUG_H

/*
 * higher verbose level, more detailed output
 * 5 - log everything
 * 3 - global allocation, yieldpoint synchronization
 * 1 - initilizer
 */
#define DEBUG_VERBOSE_LEVEL 3
#define DEBUG

#ifdef DEBUG
# define DEBUG_PRINT(l, x) 				if (DEBUG_VERBOSE_LEVEL >= l) printf x
# define uVM_assert(a, b) 				if (!(a)) uVM_fail(b)
# define uVM_assertPrintf(a, b) 		if (!(a)) {printf b; uVM_fail("assert failed");}
#else
# define DEBUG_PRINT(l, x) do {} while (0)
# define uVM_assert(a, b) do {} while (0)
#endif

// ---------------------FUNCTIONS------------------------

extern void uvmPrintInt64(int64_t);
extern void uvmPrintDouble(double);
extern void uvmPrintStr(Address);
extern void uVM_fail(const char* str);
extern void uVM_suspend(const char* str);
extern void NOT_REACHED();

#endif
