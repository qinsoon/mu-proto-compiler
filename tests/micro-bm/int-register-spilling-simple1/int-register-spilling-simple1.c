int main(void) {
  long long int v1 = 0,  v2 = 0,  v3 = 0,  v4 = 0,  v5 = 0,  v6 = 0,  v7 = 0,  v8 = 0;
  long long int v9 = 0, v10 = 0, v11 = 0, v12 = 0, v13 = 0, v14 = 0, v15 = 0, v16 = 0;

  volatile int i = 0;
  while (i < 10) {
  	v1 += i;
	v2 += v1;
	v3 += v2;
	v4 += v3;
	v5 += v4;
	v6 += v5;
	v7 += v6;
	v8 += v7;
	v9 += v8;
	v10 += v9;
	v11 += v10;
	v12 += v11;
	v13 += v12;
	v14 += v13;
	v15 += v14;
	v16 += v15;
	i++;
  }

  long long int sum = v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + \
  	v13 + v14 + v15 + v16;
  return sum;
}
