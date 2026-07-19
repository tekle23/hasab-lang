package hasab.cli.fmt

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Formats all .has source files in the project.
 */
public class FmtCommand : Command {
    override val name: String = "fmt"
    override val description: String = "Format source files"
    override val usage: String = "hasab fmt [--check] [--diff]"

    override fun execute(args: List<String>): Int {
        val checkMode = args.contains("--check")
        val diffMode = args.contains("--diff")

        val config = ProjectConfig.load()
        val srcDir = File(config.sourceDir)
        val testDir = File(config.testDir)

        val hasFiles = mutableListOf<File>()
        if (srcDir.exists()) {
            hasFiles.addAll(srcDir.walkTopDown().filter { it.extension == "has" })
        }
        if (testDir.exists()) {
            hasFiles.addAll(testDir.walkTopDown().filter { it.extension == "has" })
        }

        if (hasFiles.isEmpty()) {
            println("No .has files found to format.")
            return 0
        }

        var changedCount = 0

        for (file in hasFiles) {
            val source = file.readText(Charsets.UTF_8)
            val formatted = HasabFormatter.format(source)

            if (source != formatted) {
                changedCount++
                if (checkMode) {
                    println("  Would format: ${file.path}")
                } else if (diffMode) {
                    println("  Diff for ${file.path}:")
                    showDiff(source, formatted)
                } else {
                    file.writeText(formatted, Charsets.UTF_8)
                    println("  Formatted: ${file.path}")
                }
            }
        }

        println()
        return if (checkMode) {
            if (changedCount > 0) {
                println("$changedCount file(s) need formatting.")
                1
            } else {
                println("All files are formatted correctly.")
                0
            }
        } else {
            println("$changedCount file(s) formatted.")
            0
        }
    }

    private fun showDiff(original: String, formatted: String) {
        val origLines = original.lines()
        val fmtLines = formatted.lines()
        val maxLines = maxOf(origLines.size, fmtLines.size)
        for (i in 0 until maxLines) {
            val orig = origLines.getOrElse(i) { "" }
            val fmt = fmtLines.getOrElse(i) { "" }
            if (orig != fmt) {
                println("  - $orig")
                println("  + $fmt")
            }
        }
    }
}
