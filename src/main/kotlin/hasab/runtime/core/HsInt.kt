package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB integers.
 */
public object HsInt {

    /** The maximum value of a 32-bit signed integer. */
    public val MAX_VALUE: Int = Int.MAX_VALUE

    /** The minimum value of a 32-bit signed integer. */
    public val MIN_VALUE: Int = Int.MIN_VALUE

    /** Returns the string representation of [value]. */
    public fun toString(value: Int): String = value.toString()

    /** Parses [s] as an integer. Throws [NumberFormatException] if [s] is not a valid integer. */
    public fun parseInt(s: String): Int = s.toInt()

    /** Parses [s] as an integer, returning `null` if the parse fails. */
    public fun parseIntOrNull(s: String): Int? = s.toIntOrNull()

    /** Converts [value] to a [Char]. */
    public fun toChar(value: Int): Char = value.toChar()

    /** Converts [value] to a [Float]. */
    public fun toFloat(value: Int): Float = value.toFloat()

    /** Converts [value] to a [Long]. */
    public fun toLong(value: Int): Long = value.toLong()

    /** Converts [value] to a byte array (big-endian, 4 bytes). */
    public fun toByteArray(value: Int): ByteArray {
        return byteArrayOf(
            ((value shr 24) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            (value and 0xFF).toByte(),
        )
    }

    /** Converts a byte array (big-endian, 4 bytes) to an integer. */
    public fun fromByteArray(bytes: ByteArray): Int {
        require(bytes.size >= 4) { "Byte array must have at least 4 elements" }
        return ((bytes[0].toInt() and 0xFF) shl 24) or
            ((bytes[1].toInt() and 0xFF) shl 16) or
            ((bytes[2].toInt() and 0xFF) shl 8) or
            (bytes[3].toInt() and 0xFF)
    }

    /** Compares [a] and [b]. Returns a negative, zero, or positive integer. */
    public fun compare(a: Int, b: Int): Int = a.compareTo(b)

    /** Coerces [value] to be within [minimum] and [maximum] inclusive. */
    public fun coerceIn(value: Int, minimum: Int, maximum: Int): Int = value.coerceIn(minimum, maximum)

    /** Coerces [value] to be at least [minimum]. */
    public fun coerceAtLeast(value: Int, minimum: Int): Int = value.coerceAtLeast(minimum)

    /** Coerces [value] to be at most [maximum]. */
    public fun coerceAtMost(value: Int, maximum: Int): Int = value.coerceAtMost(maximum)

    /** Returns the individual decimal digits of [value]. */
    public fun digits(value: Int): List<Int> {
        val n = if (value == Int.MIN_VALUE) {
            return listOf(2, 1, 4, 7, 4, 8, 3, 6, 4, 8)
        } else {
            kotlin.math.abs(value)
        }
        if (n == 0) return listOf(0)
        val result = mutableListOf<Int>()
        var v = n
        while (v > 0) {
            result.add(v % 10)
            v /= 10
        }
        return result.reversed()
    }

    /** Returns `true` if [value] is even. */
    public fun isEven(value: Int): Boolean = value % 2 == 0

    /** Returns `true` if [value] is odd. */
    public fun isOdd(value: Int): Boolean = value % 2 != 0

    /** Returns `true` if [value] is positive (strictly greater than zero). */
    public fun isPositive(value: Int): Boolean = value > 0

    /** Returns `true` if [value] is negative (strictly less than zero). */
    public fun isNegative(value: Int): Boolean = value < 0

    /** Clamps [value] to the range [min]..[max]. */
    public fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)
}
