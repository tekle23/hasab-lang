package hasab.cli.docgen

import java.io.File

public class HasabDocGenerator {

    public data class DocEntry(
        public val name: String,
        public val type: String,
        public val signature: String,
        public val docComment: String,
        public val line: Int,
    )

    public fun generate(source: String, fileName: String = "input"): String {
        val entries = extractDocEntries(source)
        val sb = StringBuilder()
        sb.appendLine("# Documentation: $fileName")
        sb.appendLine()

        val functions = entries.filter { it.type == "function" }
        if (functions.isNotEmpty()) {
            sb.appendLine("## Functions")
            sb.appendLine()
            for (fn in functions) {
                sb.appendLine("### `${fn.name}`")
                sb.appendLine()
                sb.appendLine("```hasab")
                sb.appendLine(fn.signature)
                sb.appendLine("```")
                sb.appendLine()
                if (fn.docComment.isNotBlank()) {
                    sb.appendLine(fn.docComment)
                    sb.appendLine()
                }
            }
        }

        val variables = entries.filter { it.type == "variable" }
        if (variables.isNotEmpty()) {
            sb.appendLine("## Variables")
            sb.appendLine()
            for (v in variables) {
                sb.appendLine("- **`${v.name}`**: `${v.signature}`")
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    public fun extractDocEntries(source: String): List<DocEntry> {
        val lines = source.lines()
        val entries = mutableListOf<DocEntry>()
        var pendingDoc = ""

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()

            if (trimmed.startsWith("///")) {
                pendingDoc += trimmed.removePrefix("///").trimStart() + "\n"
                continue
            }

            val fnMatch = Regex("^(export\\s+)?(\u1270\u130D\u1263\u122D|fn)\\s+([\\p{L}\\p{N}_]+)\\s*\\(").find(trimmed)
            if (fnMatch != null) {
                entries.add(
                    DocEntry(
                        name = fnMatch.groupValues[3],
                        type = "function",
                        signature = trimmed,
                        docComment = pendingDoc.trim(),
                        line = index + 1,
                    )
                )
                pendingDoc = ""
                continue
            }

            val letMatch = Regex("^(export\\s+)?(\u1208|let)\\s+([\\p{L}\\p{N}_]+)").find(trimmed)
            if (letMatch != null) {
                entries.add(
                    DocEntry(
                        name = letMatch.groupValues[3],
                        type = "variable",
                        signature = trimmed,
                        docComment = pendingDoc.trim(),
                        line = index + 1,
                    )
                )
                pendingDoc = ""
                continue
            }

            if (trimmed.isNotBlank() && !trimmed.startsWith("//")) {
                pendingDoc = ""
            }
        }

        return entries
    }

    public fun generateForFile(file: File): String {
        val source = file.readText(Charsets.UTF_8)
        return generate(source, file.nameWithoutExtension)
    }

    public fun generateForDirectory(directory: File, outputFile: File): String {
        val sb = StringBuilder()
        sb.appendLine("# HASAB Project Documentation")
        sb.appendLine()

        val hasFiles = if (directory.exists()) {
            directory.walkTopDown().filter { it.extension == "has" }.toList()
        } else {
            emptyList()
        }

        for (file in hasFiles) {
            sb.appendLine("---")
            sb.appendLine()
            sb.append(generateForFile(file))
            sb.appendLine()
        }

        val content = sb.toString()
        if (outputFile.parentFile?.exists() == false) {
            outputFile.parentFile.mkdirs()
        }
        outputFile.writeText(content, Charsets.UTF_8)
        return content
    }
}
