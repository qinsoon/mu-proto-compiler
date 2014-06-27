#include <stdio.h>

int isPrime(long a) {
  for (long i = 2; i < a; i++)
    if (a % i == 0)
      return 0;

  return 1;
}

int main() {
  int sum = 0;
  for (long i = 0; i < 1001L; i++)
  	sum += isPrime(i);

  return sum;
}
