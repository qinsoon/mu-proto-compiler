package compiler.util;

public class Alignment {
	/**
	 * 
	 * @param n - the number to be aligned
	 * @param alignment - the result will be a multiply of alignment
	 * @return
	 */
    public static long alignUp(long n, long alignment) {
        return ((n - 1) & ~(alignment - 1)) + alignment;
    }

	/**
	 * 
	 * @param n - the number to be aligned
	 * @param alignment - the result will be a multiply of alignment
	 * @return
	 */
    public static long alignDown(long n, long alignment) {
        return n & ~(alignment - 1);
    }
}
