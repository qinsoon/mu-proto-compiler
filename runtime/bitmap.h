#ifndef BITMAP_H
#define BITMAP_H

#include "runtimetypes.h"

// ---------------------CONSTANTS------------------------

#define WORD_OFFSET(b) ((b) / BITS_PER_WORD)
#define BIT_OFFSET(b)  ((b) % BITS_PER_WORD)

// ---------------------FUNCTIONS------------------------

extern void set_bit(Word*, int64_t);
extern void clear_bit(Word*, int64_t);
extern int get_bit(Word*, int64_t);

#endif
