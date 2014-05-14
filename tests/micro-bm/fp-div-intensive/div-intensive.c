#define M 20

int main(void) {
  double sum = 0.0;
  int n = 0;
  while (sum < M) {
    n++;
    sum += 1.0 / n;
  }
}
