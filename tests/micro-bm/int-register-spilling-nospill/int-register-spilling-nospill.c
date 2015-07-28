int main(void) {
  long long int v1 = 0,  v2 = 0,  v3 = 0,  v4 = 0;

  volatile int i = 0;
  while (i < 10) {
  	v1 += i;
	v2 += v1;
	v3 += v2;
	v4 += v3;
	i++;
  }

  long long int sum = v1 + v2 + v3 + v4;
  return (sum % 125);
}
