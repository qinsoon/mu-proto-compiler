#ifndef UVMTYPES_H
#define UVMTYPES_H

#include <stdint.h>
#include <limits.h>
#include <stdbool.h>
#include <string.h>

typedef uint64_t Address;
typedef uint64_t Word;
typedef uint8_t  Byte;

#define WORD_SIZE sizeof(Word)
#define BITS_PER_WORD ((int64_t) (sizeof(Word) * CHAR_BIT))

#endif
