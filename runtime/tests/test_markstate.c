#include "heap.h"

#include <stdio.h>
#include <stdlib.h>

void assertEqual(Word a, Word b, char* msg) {
	if (a != b) {
		printf("assert failed, %llx is not equal to %llx: %s\n", a, b, msg);
	}
}

int main(int argc, char** argv) {
	Word markState = 0x0400000000000000L;

	Word mask      = 0x0400000000000000L;
	Word header    = 0x0000BEEF00000000L;

	Address ref = (Address) malloc(sizeof(int64_t));

	printf("mark state = %llx\n", markState);
	printf("mask       = %llx\n", mask);

	printf("header     = %llx\n", header);
	Word newHeader = newObjectHeaderWithMarkBit(header, mask, markState);
	assertEqual(newHeader, 0x0000BEEF00000000L, "new object should not be marked");
}