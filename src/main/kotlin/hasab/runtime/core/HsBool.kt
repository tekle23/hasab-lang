package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB booleans.
 */
public object HsBool {

    /** Returns the string representation of [value] (`"true"` or `"false"`). */
    public fun toString(value: Boolean): String = value.toString()

    /** Parses [s] as a boolean. Returns `true` if [s] equals `"true"` (case-insensitive). */
    public fun parseBoolean(s: String): Boolean = s.toBoolean()

    /** Returns the logical AND of [a] and [b]. */
    public fun and(a: Boolean, b: Boolean): Boolean = a && b

    /** Returns the logical OR of [a] and [b]. */
    public fun or(a: Boolean, b: Boolean): Boolean = a || b

    /** Returns the logical negation of [value]. */
    public fun not(value: Boolean): Boolean = !value

    /** Returns the logical XOR of [a] and [b]. */
    public fun xor(a: Boolean, b: Boolean): Boolean = a xor b

    /** Returns the number of `true` values in [values]. */
    public fun countTrue(vararg values: Boolean): Int = values.count { it }

    /** Returns the number of `false` values in [values]. */
    public fun countFalse(vararg values: Boolean): Int = values.count { !it }
}
