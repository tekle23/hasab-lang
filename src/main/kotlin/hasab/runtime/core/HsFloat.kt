package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB floating-point numbers.
 */
public object HsFloat {

    /** The maximum finite positive value of a 32-bit float. */
    public val MAX_VALUE: Float = Float.MAX_VALUE

    /** The minimum positive nonzero normal value of a 32-bit float. */
    public val MIN_VALUE: Float = Float.MIN_VALUE

    /** Positive infinity. */
    public val POSITIVE_INFINITY: Float = Float.POSITIVE_INFINITY

    /** Negative infinity. */
    public val NEGATIVE_INFINITY: Float = Float.NEGATIVE_INFINITY

    /** Not-a-number value. */
    public val NaN: Float = Float.NaN

    /** Returns the string representation of [value]. */
    public fun toString(value: Float): String = value.toString()

    /** Parses [s] as a float. Throws [NumberFormatException] if [s] is not a valid float. */
    public fun parseFloat(s: String): Float = s.toFloat()

    /** Parses [s] as a float, returning `null` if the parse fails. */
    public fun parseFloatOrNull(s: String): Float? = s.toFloatOrNull()

    /** Converts [value] to an integer, truncating toward zero. */
    public fun toInt(value: Float): Int = value.toInt()

    /** Returns `true` if [value] is NaN (not a number). */
    public fun isNaN(value: Float): Boolean = value.isNaN()

    /** Returns `true` if [value] is infinite (positive or negative). */
    public fun isInfinite(value: Float): Boolean = value.isInfinite()

    /** Returns `true` if [value] is finite (not NaN and not infinite). */
    public fun isFinite(value: Float): Boolean = value.isFinite()

    /** Compares [a] and [b]. Returns a negative, zero, or positive integer. */
    public fun compare(a: Float, b: Float): Int = a.compareTo(b)

    /** Coerces [value] to be within [minimum] and [maximum] inclusive. */
    public fun coerceIn(value: Float, minimum: Float, maximum: Float): Float = value.coerceIn(minimum, maximum)

    /** Coerces [value] to be at least [minimum]. */
    public fun coerceAtLeast(value: Float, minimum: Float): Float = value.coerceAtLeast(minimum)

    /** Coerces [value] to be at most [maximum]. */
    public fun coerceAtMost(value: Float, maximum: Float): Float = value.coerceAtMost(maximum)
}
