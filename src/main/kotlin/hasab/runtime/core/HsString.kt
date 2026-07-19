package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB strings.
 *
 * HASAB strings are represented as JVM [String] instances.
 * All methods in this object accept and return JVM strings.
 */
public object HsString {

    /** Returns the length of [s]. */
    public fun length(s: String): Int = s.length

    /** Returns the character at [index] in [s]. */
    public fun charAt(s: String, index: Int): Char = s[index]

    /** Returns the substring of [s] from [start] (inclusive) to [end] (exclusive). */
    public fun substring(s: String, start: Int, end: Int): String = s.substring(start, end)

    /** Returns [s] converted to upper case. */
    public fun toUpperCase(s: String): String = s.uppercase()

    /** Returns [s] converted to lower case. */
    public fun toLowerCase(s: String): String = s.lowercase()

    /** Returns [s] with leading and trailing whitespace removed. */
    public fun trim(s: String): String = s.trim()

    /** Returns `true` if [s] starts with [prefix]. */
    public fun startsWith(s: String, prefix: String): Boolean = s.startsWith(prefix)

    /** Returns `true` if [s] ends with [suffix]. */
    public fun endsWith(s: String, suffix: String): Boolean = s.endsWith(suffix)

    /** Returns `true` if [s] contains [other] as a substring. */
    public fun contains(s: String, other: String): Boolean = s.contains(other)

    /** Returns the index of the first occurrence of [other] in [s], or `-1` if not found. */
    public fun indexOf(s: String, other: String): Int = s.indexOf(other)

    /** Returns the index of the last occurrence of [other] in [s], or `-1` if not found. */
    public fun lastIndexOf(s: String, other: String): Int = s.lastIndexOf(other)

    /** Returns [s] with all occurrences of [old] replaced by [new]. */
    public fun replace(s: String, old: String, new: String): String = s.replace(old, new)

    /** Splits [s] around occurrences of [delimiter]. */
    public fun split(s: String, delimiter: String): Array<String> = s.split(delimiter).toTypedArray()

    /** Joins [parts] with [delimiter] between each element. */
    public fun join(parts: Array<String>, delimiter: String): String = parts.joinToString(delimiter)

    /** Returns `true` if [s] is empty. */
    public fun isEmpty(s: String): Boolean = s.isEmpty()

    /** Returns `true` if [s] is blank (empty or contains only whitespace). */
    public fun isBlank(s: String): Boolean = s.isBlank()

    /** Returns [s] repeated [count] times. */
    public fun repeat(s: String, count: Int): String = s.repeat(count)

    /** Returns [s] with characters in reverse order. */
    public fun reverse(s: String): String = s.reversed()

    /** Compares [a] and [b] lexicographically. Returns a negative, zero, or positive integer. */
    public fun compareTo(a: String, b: String): Int = a.compareTo(b)

    /** Returns a formatted string using [template] and [args]. */
    public fun format(template: String, vararg args: Any?): String = template.format(*args)

    /** Returns the string representation of [value]. */
    public fun valueOf(value: Any?): String = value.toString()

    /** Parses [s] as a boolean. Returns `true` if [s] equals `"true"` (case-insensitive). */
    public fun parseBoolean(s: String): Boolean = s.toBoolean()

    /** Parses [s] as an integer. Throws [NumberFormatException] if [s] is not a valid integer. */
    public fun parseInt(s: String): Int = s.toInt()

    /** Parses [s] as a float. Throws [NumberFormatException] if [s] is not a valid float. */
    public fun parseFloat(s: String): Float = s.toFloat()

    /** Converts [s] to a character array. */
    public fun toCharArray(s: String): CharArray = s.toCharArray()

    /** Encodes [s] to a UTF-8 byte array. */
    public fun encode(s: String): ByteArray = s.toByteArray(Charsets.UTF_8)

    /** Decodes a UTF-8 byte array to a string. */
    public fun decode(bytes: ByteArray): String = String(bytes, Charsets.UTF_8)

    /** Returns `true` if [s] is non-empty and contains only ASCII alphabetic characters. */
    public fun isAlpha(s: String): Boolean = s.isNotEmpty() && s.all { it.isLetter() }

    /** Returns `true` if [s] is non-empty and contains only ASCII digit characters. */
    public fun isNumeric(s: String): Boolean = s.isNotEmpty() && s.all { it.isDigit() }

    /** Returns `true` if [s] is non-empty and contains only ASCII alphabetic or digit characters. */
    public fun isAlphaNumeric(s: String): Boolean = s.isNotEmpty() && s.all { it.isLetterOrDigit() }
}
