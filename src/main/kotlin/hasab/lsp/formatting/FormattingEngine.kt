package hasab.lsp.formatting

import hasab.cli.fmt.HasabFormatter
import hasab.lsp.DocumentState
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit

public class FormattingEngine {

    public fun formatDocument(state: DocumentState): List<TextEdit> {
        val content = state.content
        val formatted = try {
            HasabFormatter.format(content)
        } catch (_: Exception) {
            return emptyList()
        }

        if (formatted == content) return emptyList()

        return listOf(
            TextEdit(
                Range(Position(0, 0), Position(Int.MAX_VALUE, Int.MAX_VALUE)),
                formatted,
            )
        )
    }

    public fun formatRange(state: DocumentState, range: Range): List<TextEdit> {
        val content = state.content
        val lines = content.lines()

        val startLine = range.start.line.coerceIn(0, lines.size - 1)
        val endLine = range.end.line.coerceIn(0, lines.size - 1)

        val selectedText = lines.subList(startLine, endLine + 1).joinToString("\n")
        val formatted = try {
            HasabFormatter.format(selectedText)
        } catch (_: Exception) {
            return emptyList()
        }

        if (formatted == selectedText) return emptyList()

        return listOf(
            TextEdit(
                Range(Position(startLine, 0), Position(endLine + 1, 0)),
                formatted,
            )
        )
    }

    public fun formatOnType(state: DocumentState, position: Position, ch: String): List<TextEdit> {
        if (ch != "\n" && ch != "}") return emptyList()

        val content = state.content
        val lines = content.lines()
        val lineIndex = position.line.coerceIn(0, lines.size - 1)
        val line = lines[lineIndex]

        val trimmed = line.trimEnd()
        if (trimmed == line) return emptyList()

        return listOf(
            TextEdit(
                Range(Position(lineIndex, 0), Position(lineIndex, line.length)),
                trimmed,
            )
        )
    }
}
