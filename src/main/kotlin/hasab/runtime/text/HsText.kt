package hasab.runtime.text

/**
 * Text/string processing utilities providing common string operations.
 */
public object HsText {

    public fun length(text: String): Int = text.length

    public fun isEmpty(text: String): Boolean = text.isEmpty()

    public fun isNotEmpty(text: String): Boolean = text.isNotEmpty()

    public fun isBlank(text: String): Boolean = text.isBlank()

    public fun isNotBlank(text: String): Boolean = text.isNotBlank()

    public fun upper(text: String): String = text.uppercase()

    public fun lower(text: String): String = text.lowercase()

    public fun capitalize(text: String): String = text.replaceFirstChar { it.titlecase() }

    public fun decapitalize(text: String): String = text.replaceFirstChar { it.lowercase() }

    public fun trim(text: String): String = text.trim()

    public fun trimStart(text: String): String = text.trimStart()

    public fun trimEnd(text: String): String = text.trimEnd()

    public fun padStart(text: String, length: Int, padChar: Char = ' '): String = text.padStart(length, padChar)

    public fun padEnd(text: String, length: Int, padChar: Char = ' '): String = text.padEnd(length, padChar)

    public fun center(text: String, length: Int, padChar: Char = ' '): String {
        if (length <= text.length) return text
        val totalPadding = length - text.length
        val leftPadding = totalPadding / 2
        val rightPadding = totalPadding - leftPadding
        return padChar.toString().repeat(leftPadding) + text + padChar.toString().repeat(rightPadding)
    }

    public fun repeat(text: String, count: Int): String = text.repeat(count)

    public fun reverse(text: String): String = text.reversed()

    public fun contains(text: String, other: String, ignoreCase: Boolean = false): Boolean = text.contains(other, ignoreCase)

    public fun startsWith(text: String, prefix: String, ignoreCase: Boolean = false): Boolean = text.startsWith(prefix, ignoreCase)

    public fun endsWith(text: String, suffix: String, ignoreCase: Boolean = false): Boolean = text.endsWith(suffix, ignoreCase)

    public fun indexOf(text: String, other: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int = text.indexOf(other, startIndex, ignoreCase)

    public fun lastIndexOf(text: String, other: String, ignoreCase: Boolean = false): Int {
        if (!ignoreCase) return text.lastIndexOf(other)
        val lowerText = text.lowercase()
        val lowerOther = other.lowercase()
        var index = lowerText.lastIndexOf(lowerOther)
        return index
    }

    public fun replace(text: String, oldValue: String, newValue: String, ignoreCase: Boolean = false): String = text.replace(oldValue, newValue, ignoreCase)

    public fun replaceFirst(text: String, oldValue: String, newValue: String, ignoreCase: Boolean = false): String = text.replaceFirst(oldValue, newValue, ignoreCase)

    public fun split(text: String, delimiter: String, limit: Int = 0): List<String> = text.split(delimiter, limit = if (limit == 0) Int.MAX_VALUE else limit)

    public fun join(parts: Iterable<Any?>, separator: String = ", "): String = parts.joinToString(separator)

    public fun substring(text: String, startIndex: Int, endIndex: Int = text.length): String = text.substring(startIndex, endIndex)

    public fun take(text: String, n: Int): String = text.take(n)

    public fun takeLast(text: String, n: Int): String = text.takeLast(n)

    public fun drop(text: String, n: Int): String = text.drop(n)

    public fun dropLast(text: String, n: Int): String = text.dropLast(n)

    public fun chunked(text: String, size: Int): List<String> = text.chunked(size)

    public fun lines(text: String): List<String> = text.lines()

    public fun lineSequence(text: String): List<String> = text.lines()

    public fun matches(text: String, regex: String): Boolean = Regex(regex).matches(text)

    public fun replaceRegex(text: String, regex: String, replacement: String): String = text.replace(Regex(regex), replacement)

    public fun findAll(text: String, regex: String): List<String> = Regex(regex).findAll(text).map { it.value }.toList()

    public fun isAlpha(text: String): Boolean = text.isNotEmpty() && text.all { it.isLetter() }

    public fun isNumeric(text: String): Boolean = text.isNotEmpty() && text.all { it.isDigit() }

    public fun isAlphaNumeric(text: String): Boolean = text.isNotEmpty() && text.all { it.isLetterOrDigit() }

    public fun isUpperCase(text: String): Boolean = text.isNotEmpty() && text.all { it.isUpperCase() }

    public fun isLowerCase(text: String): Boolean = text.isNotEmpty() && text.all { it.isLowerCase() }

    public fun compareTo(a: String, b: String, ignoreCase: Boolean = false): Int = a.compareTo(b, ignoreCase)

    public fun commonPrefix(a: String, b: String): String {
        val minLength = minOf(a.length, b.length)
        var i = 0
        while (i < minLength && a[i] == b[i]) i++
        return a.substring(0, i)
    }

    public fun commonSuffix(a: String, b: String): String {
        val minLength = minOf(a.length, b.length)
        var i = 0
        while (i < minLength && a[a.length - 1 - i] == b[b.length - 1 - i]) i++
        return a.substring(a.length - i)
    }

    public fun indent(text: String, indentation: Int = 1): String {
        val indentStr = " ".repeat(indentation)
        return text.lines().joinToString("\n") { indentStr + it }
    }

    public fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    public fun unescapeHtml(text: String): String = text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    public fun truncate(text: String, maxLength: Int, suffix: String = "..."): String {
        if (text.length <= maxLength) return text
        return text.substring(0, maxLength - suffix.length) + suffix
    }

    public fun levenshteinDistance(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }
}
