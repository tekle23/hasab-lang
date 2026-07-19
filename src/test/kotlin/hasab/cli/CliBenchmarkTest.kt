package hasab.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Benchmarks for core CLI operations.
 * Measures routing overhead and command execution time.
 */
public class CliBenchmarkTest {

    @Test
    public fun `command routing overhead is under 100ms`() {
        val router = CommandRouter(
            mapOf(
                "version" to hasab.cli.commands.VersionCommand(),
                "help" to hasab.cli.commands.HelpCommand(),
            )
        )
        val start = System.nanoTime()
        repeat(1000) {
            router.dispatch(listOf("version"))
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 5000.0, "Routing 1000 commands took ${elapsed}ms, expected < 5000ms")
    }

    @Test
    public fun `formatter handles large files efficiently`() {
        val sb = StringBuilder()
        repeat(1000) { i ->
            sb.appendLine("let var_$i = $i")
        }
        val source = sb.toString()

        val start = System.nanoTime()
        val formatted = hasab.cli.fmt.HasabFormatter.format(source)
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 5000.0, "Formatting took ${elapsed}ms, expected < 5000ms")
        assertTrue(formatted.isNotEmpty())
    }

    @Test
    public fun `linter handles large files efficiently`() {
        val sb = StringBuilder()
        repeat(500) { i ->
            sb.appendLine("/// Doc for fn $i")
            sb.appendLine("fn func_$i() {")
            sb.appendLine("    print(\"$i\")")
            sb.appendLine("}")
            sb.appendLine()
        }
        val source = sb.toString()

        val start = System.nanoTime()
        val result = hasab.cli.lint.HasabLinter.lint(source, "bench.has")
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 10000.0, "Linting took ${elapsed}ms, expected < 10000ms")
    }

    @Test
    public fun `doc generator handles many functions efficiently`() {
        val sb = StringBuilder()
        repeat(200) { i ->
            sb.appendLine("/// Function $i description")
            sb.appendLine("fn func_$i(a: int): int {")
            sb.appendLine("    return a + $i")
            sb.appendLine("}")
            sb.appendLine()
        }
        val source = sb.toString()

        val start = System.nanoTime()
        val result = hasab.cli.docgen.HasabDocGenerator().generate(source, "bench")
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 5000.0, "Doc generation took ${elapsed}ms, expected < 5000ms")
        assertTrue(result.contains("func_0"))
        assertTrue(result.contains("func_199"))
    }

    @Test
    public fun `Terminal colorize is fast`() {
        val start = System.nanoTime()
        repeat(10000) {
            Terminal.colorize(Terminal.RED, "test string")
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 2000.0, "10000 colorize calls took ${elapsed}ms, expected < 2000ms")
    }

    @Test
    public fun `HasabCli main entry point completes quickly`() {
        val start = System.nanoTime()
        HasabCli.run(arrayOf("version"))
        val elapsed = (System.nanoTime() - start) / 1_000_000.0
        assertTrue(elapsed < 2000.0, "CLI version command took ${elapsed}ms, expected < 2000ms")
    }
}
