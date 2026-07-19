package hasab.cli.config

import java.io.File

/**
 * Simple TOML parser for HASAB project configuration files.
 *
 * Supports basic TOML features: key-value pairs, strings, integers,
 * nested tables via dotted keys, and inline tables.
 */
public object HasabToml {

    /**
     * Parses a TOML file and returns a flat key-value map.
     *
     * Keys are normalized to lowercase with dots for nested access
     * (e.g. "package.name" for `[package] name = "..."`).
     */
    public fun parse(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()
        return parse(file.readText(Charsets.UTF_8))
    }

    /**
     * Parses a TOML string and returns a flat key-value map.
     */
    public fun parse(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var currentTable = ""

        for (rawLine in content.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            val tableMatch = Regex("^\\[(.+)]$").matchEntire(line)
            if (tableMatch != null) {
                currentTable = tableMatch.groupValues[1].trim()
                continue
            }

            val eqIndex = line.indexOf('=')
            if (eqIndex == -1) continue

            val key = line.substring(0, eqIndex).trim()
            val value = line.substring(eqIndex + 1).trim()

            val fullKey = if (currentTable.isNotEmpty()) "$currentTable.$key" else key
            result[fullKey.lowercase()] = parseValue(value)
        }

        return result
    }

    private fun parseValue(raw: String): String {
        val trimmed = raw.trim()
        val withoutComment = removeTrailingComment(trimmed)
        return when {
            withoutComment.startsWith("\"") && withoutComment.endsWith("\"") -> {
                withoutComment.substring(1, withoutComment.length - 1)
            }
            withoutComment.startsWith("'") && withoutComment.endsWith("'") -> {
                withoutComment.substring(1, withoutComment.length - 1)
            }
            else -> withoutComment
        }
    }

    private fun removeTrailingComment(value: String): String {
        var inString = false
        var quoteChar = ' '
        for (i in value.indices) {
            val ch = value[i]
            if (!inString && (ch == '"' || ch == '\'')) {
                inString = true
                quoteChar = ch
            } else if (inString && ch == quoteChar) {
                inString = false
            } else if (!inString && ch == '#') {
                return value.substring(0, i).trimEnd()
            }
        }
        return value
    }

    /**
     * Serializes a map to TOML format.
     */
    public fun serialize(data: Map<String, String>): String = buildString {
        var currentPrefix = ""
        val sorted = data.toSortedMap()

        for ((key, value) in sorted) {
            val parts = key.split('.')
            if (parts.size > 1) {
                val table = parts.dropLast(1).joinToString(".")
                if (table != currentPrefix) {
                    if (currentPrefix.isNotEmpty()) appendLine()
                    appendLine("[$table]")
                    currentPrefix = table
                }
                appendLine("  ${parts.last()} = \"$value\"")
            } else {
                if (currentPrefix.isNotEmpty()) {
                    appendLine()
                    currentPrefix = ""
                }
                appendLine("$key = \"$value\"")
            }
        }
    }
}
