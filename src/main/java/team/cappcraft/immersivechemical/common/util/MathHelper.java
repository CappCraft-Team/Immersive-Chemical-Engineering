package team.cappcraft.immersivechemical.common.util;

public class MathHelper {
    /**
     * Compute two value's least common multiple
     *
     * @throws ArithmeticException if parameters contains zero
     */
    public static int LeastCommonMultiple(int valA, int valB) {
        if (valA < 0 || valB < 0) throw new ArithmeticException("Only Natural number has factor");
        if (valA == 0 || valB == 0) throw new ArithmeticException("Zero has no factor");
        return valA * valB / GreatestCommonDivisor(valA, valB);
    }

    /**
     * Compute two value's greatest common divisor
     */
    public static int GreatestCommonDivisor(int valA, int valB) {
        if (valA < 0 || valB < 0) throw new ArithmeticException("Only Natural number has factor");
        if (valA == 0 || valB == 0) return Math.max(valA, valB);
        if (valA % valB == 0) return valB;
        return GreatestCommonDivisor(valB, valA % valB);
    }

    /**
     * reduce an integer's remainder
     *
     * @param dividend the integer to be dividend
     * @param divisor  the divisor
     * @return an integer that has reduced remainder
     * @throws ArithmeticException if {@param divisor} is zero
     */
    public static int ReduceRemainder(int dividend, int divisor) {
        return dividend - dividend % divisor;
    }
}
