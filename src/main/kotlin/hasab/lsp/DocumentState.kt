package hasab.lsp

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.LexerResult
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.ParseResult
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.types.TypeCheckResult
import hasab.compiler.types.TypeChecker

public enum class ChangeType {
    SEMANTIC,
    NON_SEMANTIC,
    STRUCTURAL,
}

public data class ChangeRange(
    val startLine: Int,
    val endLine: Int,
    val type: ChangeType,
)

public class DocumentState(
    public val uri: String,
    public val languageId: String,
    initialVersion: Int,
) {
    @Volatile
    public var version: Int = initialVersion
        private set

    @Volatile
    public var content: String = ""
        private set

    @Volatile
    public var lexerResult: LexerResult? = null
        private set

    @Volatile
    public var parseResult: ParseResult? = null
        private set

    @Volatile
    public var semanticModel: SemanticModel? = null
        private set

    @Volatile
    public var typeCheckResult: TypeCheckResult? = null
        private set

    private val changeHistory = mutableListOf<ChangeRange>()

    public val fileName: String
        get() = uriToFileName(uri)

    public val lastChange: ChangeRange?
        get() = changeHistory.lastOrNull()

    public val lineCount: Int
        get() = content.lines().size

    public fun updateContent(newContent: String, newVersion: Int) {
        content = newContent
        version = newVersion
    }

    public fun recordChange(startLine: Int, endLine: Int, type: ChangeType) {
        changeHistory.add(ChangeRange(startLine, endLine, type))
        if (changeHistory.size > 100) {
            changeHistory.removeFirst()
        }
    }

    public fun setLexerResult(result: LexerResult) {
        lexerResult = result
    }

    public fun setParseResult(result: ParseResult) {
        parseResult = result
    }

    public fun setSemanticModel(model: SemanticModel) {
        semanticModel = model
    }

    public fun setTypeCheckResult(result: TypeCheckResult) {
        typeCheckResult = result
    }

    public fun parse(): ParseResult {
        val src = SourceFile(fileName, content)
        val lexResult = Lexer(src).tokenize()
        setLexerResult(lexResult)
        val result = Parser(lexResult).parse()
        setParseResult(result)
        return result
    }

    public fun analyzeSemantics(): SemanticModel {
        val result = parseResult ?: parse()
        val model = SemanticAnalyzer().analyze(result.module)
        setSemanticModel(model)
        return model
    }

    public fun typeCheck(): TypeCheckResult {
        val result = parseResult ?: parse()
        val tcResult = TypeChecker().check(result.module)
        setTypeCheckResult(tcResult)
        return tcResult
    }

    public fun fullAnalysis(): FullAnalysisResult {
        val parsed = parse()
        val semantic = analyzeSemantics()
        val typed = typeCheck()
        return FullAnalysisResult(parsed, semantic, typed)
    }

    public fun invalidate() {
        lexerResult = null
        parseResult = null
        semanticModel = null
        typeCheckResult = null
    }

    public fun shouldReanalyzeSemantic(): Boolean {
        val last = changeHistory.lastOrNull() ?: return true
        return last.type != ChangeType.NON_SEMANTIC
    }

    public fun shouldReparse(): Boolean {
        val last = changeHistory.lastOrNull() ?: return true
        return last.type == ChangeType.STRUCTURAL || parseResult == null
    }

    public fun detectChangeType(oldContent: String, newContent: String): ChangeType {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()

        val minSize = minOf(oldLines.size, newLines.size)
        var hasCodeChange = false
        var hasStructuralChange = false

        for (i in 0 until minSize) {
            val oldLine = oldLines[i].trim()
            val newLine = newLines[i].trim()
            if (oldLine == newLine) continue

            if (oldLine.startsWith("//") && newLine.startsWith("//")) continue
            if (oldLine.isEmpty() && newLine.isEmpty()) continue

            val oldStripped = oldLine.removeSuffix("}")
            val newStripped = newLine.removeSuffix("}")
            val oldOpenBraces = oldLine.count { it == '{' }
            val newOpenBraces = newLine.count { it == '{' }
            val oldCloseBraces = oldLine.count { it == '}' }
            val newCloseBraces = newLine.count { it == '}' }
            if (oldOpenBraces != newOpenBraces || oldCloseBraces != newCloseBraces) {
                hasStructuralChange = true
            }

            hasCodeChange = true
        }

        if (!hasCodeChange && oldLines.size == newLines.size) return ChangeType.NON_SEMANTIC
        if (!hasCodeChange && oldLines.size != newLines.size) {
            val addedLines = newLines.drop(oldLines.size)
            val removedLines = oldLines.drop(newLines.size)
            val nonCommentAdded = addedLines.any { it.trim().isNotEmpty() && !it.trim().startsWith("//") }
            val nonCommentRemoved = removedLines.any { it.trim().isNotEmpty() && !it.trim().startsWith("//") }
            if (!nonCommentAdded && !nonCommentRemoved) return ChangeType.NON_SEMANTIC
        }

        return if (hasStructuralChange) ChangeType.STRUCTURAL else ChangeType.SEMANTIC
    }

    public fun getChangeRange(oldContent: String, newContent: String): Pair<Int, Int> {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()
        val minSize = minOf(oldLines.size, newLines.size)

        var startLine = 0
        for (i in 0 until minSize) {
            if (oldLines[i] != newLines[i]) {
                startLine = i
                break
            }
            if (i == minSize - 1) {
                startLine = minSize
            }
        }

        var endLine = 0
        for (i in 1..minSize) {
            val oldIdx = oldLines.size - i
            val newIdx = newLines.size - i
            if (oldIdx < 0 || newIdx < 0) break
            if (oldLines[oldIdx] != newLines[newIdx]) {
                endLine = maxOf(oldLines.size, newLines.size) - i
                break
            }
        }

        return Pair(startLine, endLine)
    }

    public data class FullAnalysisResult(
        val parseResult: ParseResult,
        val semanticModel: SemanticModel,
        val typeCheckResult: TypeCheckResult,
    )

    public companion object {
        public fun uriToFileName(uri: String): String {
            return if (uri.startsWith("file:///")) {
                uri.removePrefix("file:///").replace("/", if (System.getProperty("os.name").lowercase().contains("win")) "\\" else "/")
            } else {
                uri
            }
        }
    }
}
