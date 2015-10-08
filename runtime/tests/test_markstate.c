#include "heap.h"

#include <stdio.h>
#include <stdlib.h>

void assertEqual(Word a, Word b, char* msg) {
	if (a != b) {
		printf("assert failed, %016llx is not equal to %016llx: %s\n", a, b, msg);
	}
}

int main(int argc, char** argv) {
	Word markState = 0x0400000000000000L;

	Word mask      = 0x0400000000000000L;
	Word header    = 0x0000BEEF00000000L;

	Address ref = (Address) malloc(sizeof(int64_t));

	printf("mark state = %016llx\n", markState);
	printf("mask       = %016llx\n", mask);
	printf("header     = %016llx\n", header);
	
	printf("Test 1: new object should have a header without mark bit marked\n");
	Word newHeader = newObjectHeaderWithMarkBit(header, mask, markState);
	assertEqual(newHeader, 0x0000BEEF00000000L, "new object should not be marked");
	*((Word*)ref) = newHeader;
	
	printf("Test 2: mark object\n");
	setMarkBitInHeader(ref, mask, markState);
	assertEqual(*((Word*)ref), 0x0400BEEF00000000L, "the object should be marked with 1 in mark bit");
	
	printf("Test 3: test if marked object is marked\n");
	bool marked = testMarkBitInHeader(ref, mask, markState);
	if (!marked) {
		printf("assert failed, the object is marked, but testMarkBitInHeader() returned false\n");
	}
	
	printf("Test 4: flip mark state\n");
	flipBit(mask, &markState);
	assertEqual(markState, 0, "mark state doesnt get flipped");
	
	printf("Test 5: new object should have a header without mark bit marked\n");
	newHeader = newObjectHeaderWithMarkBit(header, mask, markState);
	assertEqual(newHeader, 0x0400BEEF00000000L, "new object should not be marked");
	*((Word*)ref) = newHeader;
	
	printf("Test 6: mark object\n");
	setMarkBitInHeader(ref, mask, markState);
	assertEqual(*((Word*)ref), 0x0000BEEF00000000L, "the object should be marked with 0 in mark bit");
	
	printf("Test 7: test if marked object is marked\n");
	marked = testMarkBitInHeader(ref, mask, markState);
	if (!marked) {
		printf("assert failed, the object is marked, but testMarkBitInHeader() returned false\n");
	}
	
	printf("Test 8: flip mark state\n");
	flipBit(mask, &markState);
	assertEqual(markState, 0x0400000000000000, "mark state doesnt get flipped");
}