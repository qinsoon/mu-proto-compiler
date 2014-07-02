#include <stdio.h>

extern int isPrime(long);

int main() {
  int sum = 0;
  for (long i = 2; i < 100001L; i++)
  	sum += isPrime(i);

  return sum;
}
