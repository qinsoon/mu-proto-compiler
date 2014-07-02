int isPrime(long a) {
  for (long i = 2; i < a; i++)
      if (a % i == 0)
            return 0;
  return 1;
}
