#include "bitmap.h"

/*
 * bit map
 */
void set_bit(Word *words, int64_t n) {
    words[WORD_OFFSET(n)] |= (1L << BIT_OFFSET(n));
}

void clear_bit(Word *words, int64_t n) {
    words[WORD_OFFSET(n)] &= ~(1L << BIT_OFFSET(n));
}

int get_bit(Word *words, int64_t n) {
    Word bit = words[WORD_OFFSET(n)] & (1L << BIT_OFFSET(n));
    return bit != 0;
}
