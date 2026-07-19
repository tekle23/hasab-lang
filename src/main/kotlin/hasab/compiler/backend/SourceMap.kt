package hasab.compiler.backend

public data class SourceLocation(
    val file: String,
    val line: Int,
    val column: Int,
    val endLine: Int = line,
    val endColumn: Int = column,
)

public class SourceMap {

    private val generatedToSource = mutableMapOf<String, MutableMap<Int, SourceLocation>>()
    private val classToSourceFile = mutableMapOf<String, String>()
    private val sourceFileLineOffsets = mutableMapOf<String, Int>()

    public fun record(
        generatedFile: String,
        generatedLine: Int,
        sourceFile: String,
        sourceLine: Int,
        sourceCol: Int,
        sourceEndLine: Int = sourceLine,
        sourceEndCol: Int = sourceCol,
    ) {
        val lineMap = generatedToSource.getOrPut(generatedFile) { mutableMapOf() }
        lineMap[generatedLine] = SourceLocation(sourceFile, sourceLine, sourceCol, sourceEndLine, sourceEndCol)
    }

    public fun recordClassMapping(className: String, sourceFile: String) {
        classToSourceFile[className] = sourceFile
    }

    public fun recordSourceFileLineOffset(sourceFile: String, offset: Int) {
        sourceFileLineOffsets[sourceFile] = offset
    }

    public fun getGeneratedToSourceMap(generatedFile: String): Map<Int, SourceLocation> {
        return generatedToSource[generatedFile]?.toMap() ?: emptyMap()
    }

    public fun translateCompileError(generatedFile: String, generatedLine: Int): SourceLocation? {
        val lineMap = generatedToSource[generatedFile] ?: return null
        return findNearestLine(lineMap, generatedLine)
    }

    public fun translateRuntimeTrace(className: String, javaLine: Int): SourceLocation? {
        val sourceFile = classToSourceFile[className] ?: return null
        val offset = sourceFileLineOffsets[sourceFile] ?: 0
        return SourceLocation(sourceFile, javaLine + offset, 0)
    }

    public fun translateRuntimeTraceAll(className: String, javaLine: Int): List<SourceLocation> {
        val sourceFile = classToSourceFile[className] ?: return emptyList()
        val offset = sourceFileLineOffsets[sourceFile] ?: 0
        val primary = SourceLocation(sourceFile, javaLine + offset, 0)
        val candidates = mutableListOf(primary)

        val lineMap = generatedToSource.values.flatMap { it.values }
            .filter { it.file == sourceFile }
        for (loc in lineMap) {
            if (kotlin.math.abs(loc.line - (javaLine + offset)) <= 2) {
                candidates.add(loc)
            }
        }
        return candidates.distinctBy { "${it.file}:${it.line}" }
    }

    public val size: Int
        get() = generatedToSource.values.sumOf { it.size }

    private fun findNearestLine(lineMap: Map<Int, SourceLocation>, targetLine: Int): SourceLocation? {
        if (lineMap.isEmpty()) return null
        var bestLine = -1
        var bestDist = Int.MAX_VALUE
        for (line in lineMap.keys) {
            val dist = kotlin.math.abs(line - targetLine)
            if (dist < bestDist) {
                bestDist = dist
                bestLine = line
            }
        }
        return lineMap[bestLine]
    }
}
