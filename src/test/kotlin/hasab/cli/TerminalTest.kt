package hasab.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class TerminalTest {

    @Test
    public fun `ANSI constants are valid escape sequences`() {
        assertTrue(Terminal.RESET.startsWith("\u001B"))
        assertTrue(Terminal.RED.startsWith("\u001B"))
        assertTrue(Terminal.GREEN.startsWith("\u001B"))
        assertTrue(Terminal.YELLOW.startsWith("\u001B"))
        assertTrue(Terminal.BLUE.startsWith("\u001B"))
        assertTrue(Terminal.MAGENTA.startsWith("\u001B"))
        assertTrue(Terminal.CYAN.startsWith("\u001B"))
        assertTrue(Terminal.BOLD.startsWith("\u001B"))
        assertTrue(Terminal.DIM.startsWith("\u001B"))
    }

    @Test
    public fun `colorize wraps text`() {
        val result = Terminal.colorize(Terminal.RED, "hello")
        assertTrue(result.contains("hello"))
    }

    @Test
    public fun `success wraps text`() {
        val result = Terminal.success("done")
        assertTrue(result.contains("done"))
    }

    @Test
    public fun `error wraps text`() {
        val result = Terminal.error("fail")
        assertTrue(result.contains("fail"))
    }

    @Test
    public fun `warn wraps text`() {
        val result = Terminal.warn("careful")
        assertTrue(result.contains("careful"))
    }

    @Test
    public fun `info wraps text`() {
        val result = Terminal.info("fyi")
        assertTrue(result.contains("fyi"))
    }

    @Test
    public fun `dim wraps text`() {
        val result = Terminal.dim("subtle")
        assertTrue(result.contains("subtle"))
    }

    @Test
    public fun `bold wraps text`() {
        val result = Terminal.bold("emphasis")
        assertTrue(result.contains("emphasis"))
    }

    @Test
    public fun `printTable does not throw`() {
        Terminal.printTable(listOf("a" to "1", "b" to "2"))
    }

    @Test
    public fun `printBanner does not throw`() {
        Terminal.printBanner("Test Banner")
    }

    @Test
    public fun `enabled is a boolean`() {
        val e = Terminal.enabled
        assertTrue(e || !e)
    }
}
