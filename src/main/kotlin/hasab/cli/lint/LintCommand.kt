package hasab.cli.lint

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Lints all .has source files for common issues.
 */
public class LintCommand : Command {
    override val name: String = "lint"
    override val description: String = "Lint source files for issues"
    override val usage: String = "hasab lint [--fix]"

    override fun execute(args: List<String>): Int {
        val config = ProjectConfig.load()
        val srcDir = File(config.sourceDir)
        val testDir = File(config.testDir)

        val dirs = listOf(srcDir, testDir).filter { it.exists() }
        if (dirs.isEmpty()) {
            println("No source directories found.")
            return 0
        }

        var totalErrors = 0
        var totalWarnings = 0
        var totalInfos = 0
        var filesWithIssues = 0

        for (dir in dirs) {
            val results = HasabLinter.lintDirectory(dir)
            for (result in results) {
                if (result.hasIssues) {
                    filesWithIssues++
                    println("  ${result.file}")
                    for (issue in result.issues) {
                        val icon = when (issue.severity) {
                            LintSeverity.ERROR -> "error"
                            LintSeverity.WARNING -> "warning"
                            LintSeverity.INFO -> "info"
                        }
                        println("    ${issue.line}:${issue.column} [$icon] ${issue.rule}: ${issue.message}")
                    }
                    println()
                }
                totalErrors += result.errorCount
                totalWarnings += result.warningCount
                totalInfos += result.infoCount
            }
        }

        println("$totalErrors error(s), $totalWarnings warning(s), $totalInfos info(s)")
        println("$filesWithIssues file(s) with issues")

        return if (totalErrors > 0) 1 else 0
    }
}
