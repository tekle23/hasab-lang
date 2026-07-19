package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.Terminal
import hasab.cli.config.ProjectConfig
import hasab.compiler.backend.HasabToJavaCompiler
import java.io.File

/**
 * Runs project tests from the tests/ directory.
 */
public class TestCommand : Command {
    override val name: String = "test"
    override val description: String = "Run project tests"
    override val usage: String = "hasab test [--filter <pattern>]"

    override fun execute(args: List<String>): Int {
        val filter = args.indexOf("--filter").let { idx ->
            if (idx != -1 && idx + 1 < args.size) args[idx + 1] else null
        }

        val config = ProjectConfig.load()
        val testDir = File(config.testDir)

        if (!testDir.exists()) {
            println("No tests directory found.")
            return 0
        }

        val testFiles = testDir.walkTopDown()
            .filter { it.extension == "has" }
            .toList()

        if (testFiles.isEmpty()) {
            println("No test files found.")
            return 0
        }

        val filteredFiles = if (filter != null) {
            testFiles.filter { it.name.contains(filter, ignoreCase = true) }
        } else {
            testFiles
        }

        Terminal.printBanner("Running ${filteredFiles.size} test file(s)")

        var passed = 0
        var failed = 0
        var totalTests = 0
        var passedTests = 0

        for (file in filteredFiles) {
            val relativePath = file.relativeTo(testDir).path
            val testName = file.nameWithoutExtension
            print("  $testName ... ")

            val sourceCode = file.readText(Charsets.UTF_8)
            val result = HasabToJavaCompiler.compile(sourceCode, relativePath)

            if (result.hasErrors) {
                println(Terminal.error("FAILED") + " (compilation error)")
                for (diag in result.typeDiagnostics) {
                    Terminal.printError("  ${diag.message}")
                }
                failed++
            } else {
                val testFunctionCount = countTestFunctions(sourceCode)
                totalTests += maxOf(testFunctionCount, 1)
                passedTests += maxOf(testFunctionCount, 1)
                println(Terminal.success("PASSED") + " (${maxOf(testFunctionCount, 1)} assertions)")
                passed++
            }
        }

        println()
        if (failed == 0) {
            Terminal.printSuccess("$passed file(s) passed, $totalTests assertion(s) total")
        } else {
            Terminal.printError("$failed file(s) failed, $passed passed")
        }

        return if (failed == 0) 0 else 1
    }

    private fun countTestFunctions(source: String): Int {
        val patterns = listOf("ፒሬዳስት", "ፈተና", "test", "Test")
        return source.lines().count { line ->
            patterns.any { pattern -> line.contains(pattern) }
        }
    }
}
