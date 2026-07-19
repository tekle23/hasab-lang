package hasab.cli.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class HelpCommandTest {

    @Test
    public fun `help command has correct name`() {
        assertEquals("help", HelpCommand().name)
    }

    @Test
    public fun `help with no args returns 0`() {
        val result = HelpCommand().execute(emptyList())
        assertEquals(0, result)
    }

    @Test
    public fun `help with unknown command returns non-zero`() {
        val result = HelpCommand().execute(listOf("nonexistent"))
        assertTrue(result != 0)
    }

    @Test
    public fun `help for build command returns 0`() {
        val result = HelpCommand().execute(listOf("build"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for new command returns 0`() {
        val result = HelpCommand().execute(listOf("new"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for remove command returns 0`() {
        val result = HelpCommand().execute(listOf("remove"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for publish command returns 0`() {
        val result = HelpCommand().execute(listOf("publish"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for fmt command returns 0`() {
        val result = HelpCommand().execute(listOf("fmt"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for lint command returns 0`() {
        val result = HelpCommand().execute(listOf("lint"))
        assertEquals(0, result)
    }

    @Test
    public fun `help for doc command returns 0`() {
        val result = HelpCommand().execute(listOf("doc"))
        assertEquals(0, result)
    }
}
