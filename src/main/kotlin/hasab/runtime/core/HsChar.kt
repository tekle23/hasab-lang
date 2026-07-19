package hasab.runtime.core

/**
 * Static utility methods for operating on HASAB characters.
 */
public object HsChar {

    /** Returns the string representation of [value]. */
    public fun toString(value: Char): String = value.toString()

    /** Returns the integer (Unicode code point) value of [value]. */
    public fun toInt(value: Char): Int = value.code

    /** Returns the character for the given Unicode code point [value]. */
    public fun fromInt(value: Int): Char = value.toChar()

    /** Returns `true` if [value] is a letter. */
    public fun isLetter(value: Char): Boolean = value.isLetter()

    /** Returns `true` if [value] is a digit. */
    public fun isDigit(value: Char): Boolean = value.isDigit()

    /** Returns `true` if [value] is a letter or digit. */
    public fun isLetterOrDigit(value: Char): Boolean = value.isLetterOrDigit()

    /** Returns `true` if [value] is an upper-case letter. */
    public fun isUpperCase(value: Char): Boolean = value.isUpperCase()

    /** Returns `true` if [value] is a lower-case letter. */
    public fun isLowerCase(value: Char): Boolean = value.isLowerCase()

    /** Returns [value] converted to upper case. */
    public fun toUpperCase(value: Char): Char = value.uppercaseChar()

    /** Returns [value] converted to lower case. */
    public fun toLowerCase(value: Char): Char = value.lowercaseChar()

    /** Returns `true` if [value] is a whitespace character. */
    public fun isWhitespace(value: Char): Boolean = value.isWhitespace()

    /** Returns `true` if [value] is a control character (Unicode category Cc). */
    public fun isControl(value: Char): Boolean = value.isISOControl()

    /** Returns `true` if [value] is a printable character (not a control character). */
    public fun isPrintable(value: Char): Boolean = !value.isISOControl() && !value.isWhitespace()

    /** Compares [a] and [b] by their Unicode code points. Returns a negative, zero, or positive integer. */
    public fun compare(a: Char, b: Char): Int = a.compareTo(b)
}
