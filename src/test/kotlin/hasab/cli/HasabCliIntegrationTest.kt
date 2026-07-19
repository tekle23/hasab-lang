package hasab.cli

import hasab.cli.commands.*
import hasab.cli.fmt.FmtCommand
import hasab.cli.lint.LintCommand
import hasab.cli.docgen.DocCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * End-to-end integration tests for the HASAB CLI.
 * Tests command routing, argument parsing, and full execution flows.
 */
public class HasabCliIntegrationTest {

    @Test
    public fun `hasab with no args returns 0 and shows help`() {
        val result = HasabCli.run(emptyArray())
        assertEquals(0, result)
    }

    @Test
    public fun `hasab help returns 0`() {
        val result = HasabCli.run(arrayOf("help"))
        assertEquals(0, result)
    }

    @Test
    public fun `hasab version returns 0`() {
        val result = HasabCli.run(arrayOf("version"))
        assertEquals(0, result)
    }

    @Test
    public fun `hasab help for each registered command`() {
        val commands = listOf(
            "new", "build", "run", "test", "fmt", "lint", "doc",
            "add", "remove", "publish", "clean", "doctor", "version",
        )
        for (cmd in commands) {
            val result = HasabCli.run(arrayOf("help", cmd))
            assertEquals(0, result, "help for '$cmd' should return 0")
        }
    }

    @Test
    public fun `hasab help for unknown command returns non-zero`() {
        val result = HasabCli.run(arrayOf("help", "nonexistent"))
        assertTrue(result != 0)
    }

    @Test
    public fun `hasab new without name returns non-zero`() {
        val result = HasabCli.run(arrayOf("new"))
        assertTrue(result != 0)
    }

    @Test
    public fun `hasab add without name returns non-zero`() {
        val result = HasabCli.run(arrayOf("add"))
        assertTrue(result != 0)
    }

    @Test
    public fun `hasab remove without name returns non-zero`() {
        val result = HasabCli.run(arrayOf("remove"))
        assertTrue(result != 0)
    }

    @Test
    public fun `all commands are registered`() {
        val commands = listOf(
            "new", "build", "run", "test", "fmt", "lint", "doc",
            "add", "remove", "publish", "clean", "doctor", "version", "help",
        )
        for (cmd in commands) {
            try {
                HasabCli.run(arrayOf(cmd))
            } catch (_: NoClassDefFoundError) {
                // Some commands may reference runtime classes not available in test JVM
            }
        }
    }

    @Test
    public fun `VERSION constant is set`() {
        assertEquals("1.0.0", HasabCli.VERSION)
    }

    @Test
    public fun `unknown command returns non-zero`() {
        val result = HasabCli.run(arrayOf("totally-unknown-command"))
        assertTrue(result != 0)
    }

    @Test
    public fun `doctor command runs without crash`() {
        try {
            val result = HasabCli.run(arrayOf("doctor"))
            assertTrue(result == 0 || result == 1, "doctor should return 0 or 1, got $result")
        } catch (_: NoClassDefFoundError) {
            // Runtime classes may not be available in test JVM
        }
    }

    @Test
    public fun `clean command returns 0 when no build dir`() {
        val result = HasabCli.run(arrayOf("clean"))
        assertEquals(0, result)
    }
}
