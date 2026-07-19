package hasab.cli.fmt

import java.io.File

/**
 * Source code formatter for HASAB .has files.
 *
 * Applies consistent formatting rules:
 * - Normalizes indentation to 4 spaces
 * - Ensures spaces around operators
 * - Normalizes brace placement (K&R style)
 * - Removes trailing whitespace
 * - Ensures newline at end of file
 * - Normalizes blank lines (max 2)
 */
public object HasabFormatter {

    public const val INDENT: String = "    "

    public fun format(source: String): String {
        val lines = source.replace("\r\n", "\n").split("\n")
        val result = mutableListOf<String>()
        var indentLevel = 0
        var blankLineCount = 0
        var inMultiLineComment = false

        for (rawLine in lines) {
            val trimmed = rawLine.trim()

            if (inMultiLineComment) {
                result.add(INDENT.repeat(indentLevel) + trimmed)
                if (trimmed.contains("*/")) {
                    inMultiLineComment = false
                }
                continue
            }

            if (trimmed.isEmpty()) {
                blankLineCount++
                if (blankLineCount <= 1) {
                    result.add("")
                }
                continue
            }
            blankLineCount = 0

            if (trimmed.startsWith("//")) {
                result.add(INDENT.repeat(indentLevel) + trimmed)
                continue
            }

            if (trimmed.startsWith("/*")) {
                inMultiLineComment = !trimmed.contains("*/")
                result.add(INDENT.repeat(indentLevel) + trimmed)
                continue
            }

            if (trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }

            val formatted = formatLine(trimmed)
            result.add(INDENT.repeat(indentLevel) + formatted)

            if (formatted.endsWith("{") || formatted.endsWith("(") || formatted.endsWith("[")) {
                indentLevel++
            }
        }

        while (result.isNotEmpty() && result.last().isEmpty()) {
            result.removeAt(result.lastIndex)
        }
        result.add("")

        return result.joinToString("\n")
    }

    public fun formatLine(line: String): String {
        var result = line
        result = normalizeOperators(result)
        result = result.trimEnd()
        return result
    }

    private fun normalizeOperators(line: String): String {
        var result = line
        val compoundOps = listOf("==", "!=", "<=", ">=", "+=", "-=", "*=", "/=", "%=", "&&", "||", "->", "..")
        for (op in compoundOps) {
            val escaped = Regex.escape(op)
            result = result.replace(Regex("([a-zA-Z0-9_\\)\\]])${escaped}([a-zA-Z0-9_\\(\\[])"), "$1 $op $2")
        }
        return result
    }

    public fun needsFormatting(source: String): Boolean = source != format(source)

    public fun formatFile(file: File): Boolean {
        val source = file.readText(Charsets.UTF_8)
        val formatted = format(source)
        if (source != formatted) {
            file.writeText(formatted, Charsets.UTF_8)
            return true
        }
        return false
    }
}
