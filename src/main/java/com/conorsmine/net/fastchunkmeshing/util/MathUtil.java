package com.conorsmine.net.fastchunkmeshing.util;

public class MathUtil {

    /**
     * <p>Calculates:</p>
     * <pre>2 ^ pow</pre>
     * @param pow Power of 2; Must be <64
     * @return A power of two
     */
    public static long twosPower(int pow) {
        if (pow >= 64) throw new UnsupportedOperationException("Does not support powers larger than 63. Requested power was " + pow);
        return 1L << pow;
    }
}
