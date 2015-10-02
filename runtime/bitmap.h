#include <stdint.h>
#include <limits.h>

// bit map
typedef uint64_t Word;
#define WORD_SIZE sizeof(Word)

#define BITS_PER_WORD ((int64_t) (sizeof(Word) * CHAR_BIT))
#define WORD_OFFSET(b) ((b) / BITS_PER_WORD)
#define BIT_OFFSET(b)  ((b) % BITS_PER_WORD)

extern void set_bit(Word*, int64_t);
extern void clear_bit(Word*, int64_t);
extern int get_bit(Word*, int64_t);
