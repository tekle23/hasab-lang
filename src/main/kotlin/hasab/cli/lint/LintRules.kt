package hasab.cli.lint

public interface LintRule {
    public val id: String
    public val description: String
    public val defaultSeverity: LintSeverity
    public fun check(lines: List<String>, fileName: String): List<LintIssue>
}

internal object UnusedVariableRule : LintRule {
    override val id: String = "unused-variable"
    override val description: String = "Variables that are assigned but never used"
    override val defaultSeverity: LintSeverity = LintSeverity.WARNING

    private val assignmentPattern = Regex("^\\s*(\u1208|let)\\s+([\\p{L}\\p{N}_]+)\\s*=")
    private val identifierPattern = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b")

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val declaredVars = mutableMapOf<String, Int>()

        for ((index, line) in lines.withIndex()) {
            val match = assignmentPattern.find(line)
            if (match != null) {
                val varName = match.groupValues[2]
                declaredVars[varName] = index + 1
            }
        }

        val allText = lines.joinToString("\n")
        for ((varName, lineNum) in declaredVars) {
            val count = identifierPattern.findAll(allText).count { it.value == varName }
            if (count <= 1) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        column = 1,
                        severity = defaultSeverity,
                        rule = id,
                        message = "Variable '$varName' is declared but never used",
                    )
                )
            }
        }
        return issues
    }
}

internal object EmptyBlockRule : LintRule {
    override val id: String = "empty-block"
    override val description: String = "Empty code blocks"
    override val defaultSeverity: LintSeverity = LintSeverity.WARNING

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.endsWith("{") && index + 1 < lines.size) {
                val nextTrimmed = lines[index + 1].trim()
                if (nextTrimmed == "}") {
                    issues.add(
                        LintIssue(
                            line = index + 1,
                            column = 1,
                            severity = defaultSeverity,
                            rule = id,
                            message = "Empty block detected",
                        )
                    )
                }
            }
        }
        return issues
    }
}

internal object TrailingWhitespaceRule : LintRule {
    override val id: String = "trailing-whitespace"
    override val description: String = "Trailing whitespace"
    override val defaultSeverity: LintSeverity = LintSeverity.INFO

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        for ((index, line) in lines.withIndex()) {
            if (line != line.trimEnd()) {
                issues.add(
                    LintIssue(
                        line = index + 1,
                        column = line.trimEnd().length + 1,
                        severity = defaultSeverity,
                        rule = id,
                        message = "Trailing whitespace",
                    )
                )
            }
        }
        return issues
    }
}

internal object LongLineRule : LintRule {
    override val id: String = "long-line"
    override val description: String = "Lines exceeding 120 characters"
    override val defaultSeverity: LintSeverity = LintSeverity.INFO
    private const val MAX_LENGTH: Int = 120

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        for ((index, line) in lines.withIndex()) {
            if (line.length > MAX_LENGTH && !line.trimStart().startsWith("//")) {
                issues.add(
                    LintIssue(
                        line = index + 1,
                        column = MAX_LENGTH + 1,
                        severity = defaultSeverity,
                        rule = id,
                        message = "Line exceeds $MAX_LENGTH characters (${line.length})",
                    )
                )
            }
        }
        return issues
    }
}

internal object MissingDocCommentRule : LintRule {
    override val id: String = "missing-doc"
    override val description: String = "Public functions without documentation"
    override val defaultSeverity: LintSeverity = LintSeverity.INFO

    private val pubFnPattern = Regex("^\\s*(\u1320\u1245\u120B\u120B|pub)\\s+(\u1270\u130D\u1263\u122D|fn)\\s+([\\p{L}\\p{N}_]+)")
    private val docCommentPattern = Regex("^\\s*///")

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        for ((index, line) in lines.withIndex()) {
            if (pubFnPattern.containsMatchIn(line)) {
                val hasDoc = index > 0 && docCommentPattern.containsMatchIn(lines[index - 1])
                if (!hasDoc) {
                    issues.add(
                        LintIssue(
                            line = index + 1,
                            column = 1,
                            severity = defaultSeverity,
                            rule = id,
                            message = "Public function without doc comment",
                        )
                    )
                }
            }
        }
        return issues
    }
}

internal object ShadowVariableRule : LintRule {
    override val id: String = "shadow-variable"
    override val description: String = "Variable shadows an outer-scope variable"
    override val defaultSeverity: LintSeverity = LintSeverity.WARNING

    private val letPattern = Regex("^\\s*(\u1208|let)\\s+([\\p{L}\\p{N}_]+)")

    override fun check(lines: List<String>, fileName: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val seen = mutableSetOf<String>()

        for ((index, line) in lines.withIndex()) {
            val match = letPattern.find(line)
            if (match != null) {
                val varName = match.groupValues[2]
                if (varName in seen) {
                    issues.add(
                        LintIssue(
                            line = index + 1,
                            column = 1,
                            severity = defaultSeverity,
                            rule = id,
                            message = "Variable '$varName' shadows a previously declared variable",
                        )
                    )
                }
                seen.add(varName)
            }
        }
        return issues
    }
}
