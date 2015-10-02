#include "runtime.h"

#define SIZE (128)

void assertEqual(int64_t a, int64_t b, int64_t index) {
	if (a != b) {
		printf("NOT EQUAL for index %lld: a = %lld, b = %lld\n", index, a, b);
	}
}

void printBitmap(Word* bitmap, int size) {
	for (int i = 0; i < size; i++) {
		printf("%016llx ", bitmap[i]);
	}
	printf("\n");
}

int main(int argc, char** argv) {

	Word* bitmap = (Word*) malloc(SIZE / 8);
	memset(bitmap, 0, SIZE / 8);

	for (int64_t i = 0; i < SIZE; i++) {
		set_bit(bitmap, i);
		printBitmap(bitmap, SIZE / 64);

		for (int j = 0; j < SIZE / 64; j++) {
			Word w = bitmap[j];
			if (j == WORD_OFFSET(i)) {
				assertEqual(w, 1L << BIT_OFFSET(i), i);
			} else {
				assertEqual(w, 0L, i);
			}
		}

		int bit = get_bit(bitmap, i);
		assertEqual(bit, 1, i);

		clear_bit(bitmap, i);
		printBitmap(bitmap, SIZE / 64);
		for (int j = 0; j < SIZE / 64; j++) {
			Word w = bitmap[j];
			assertEqual(w, 0L, i);
		}
	}
}
