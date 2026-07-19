package hasab.cli.commands

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class NewCommandTest {

    @Test
    public fun `new command has correct name`() {
        val cmd = NewCommand()
        assertEquals("new", cmd.name)
    }

    @Test
    public fun `new fails without project name`() {
        val cmd = NewCommand()
        val result = cmd.execute(emptyList())
        assertTrue(result != 0)
    }

    @Test
    public fun `new fails when directory already exists`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_new_${System.nanoTime()}")
        dir.mkdirs()
        try {
            val cmd = NewCommand()
            val result = cmd.execute(listOf(dir.absolutePath))
            assertTrue(result != 0, "Should fail when directory already exists")
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `new command description is set`() {
        val cmd = NewCommand()
        assertTrue(cmd.description.isNotEmpty())
    }

    @Test
    public fun `new command usage mentions template`() {
        val cmd = NewCommand()
        assertTrue(cmd.usage.contains("--template"))
    }
}
