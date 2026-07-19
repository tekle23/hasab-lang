package hasab.cli.lint

import java.io.File

public data class LintResult(
    public val file: String,
    public val issues: List<LintIssue>,
) {
    public val hasIssues: Boolean get() = issues.isNotEmpty()
    public val errorCount: Int get() = issues.count { it.severity == LintSeverity.ERROR }
    public val warningCount: Int get() = issues.count { it.severity == LintSeverity.WARNING }
    public val infoCount: Int get() = issues.count { it.severity == LintSeverity.INFO }
}

public data class LintIssue(
    public val line: Int,
    public val column: Int,
    public val severity: LintSeverity,
    public val rule: String,
    public val message: String,
)

public enum class LintSeverity {
    ERROR,
    WARNING,
    INFO,
}

public object HasabLinter {

    private val rules: List<LintRule> = listOf(
        UnusedVariableRule,
        EmptyBlockRule,
        TrailingWhitespaceRule,
        LongLineRule,
        MissingDocCommentRule,
        ShadowVariableRule,
    )

    public fun lint(source: String, fileName: String = "<input>"): LintResult {
        val issues = mutableListOf<LintIssue>()
        val lines = source.lines()
        for (rule in rules) {
            issues.addAll(rule.check(lines, fileName))
        }
        return LintResult(fileName, issues.sortedBy { it.line })
    }

    public fun lintDirectory(directory: File): List<LintResult> {
        if (!directory.exists()) return emptyList()
        return directory.walkTopDown()
            .filter { it.extension == "has" }
            .map { file ->
                val source = file.readText(Charsets.UTF_8)
                lint(source, file.path)
            }
            .toList()
    }
}
