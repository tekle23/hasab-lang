package hasab.cli.commands

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class RemoveCommandTest {

    @Test
    public fun `remove command has correct name`() {
        assertEquals("remove", RemoveCommand().name)
    }

    @Test
    public fun `remove fails without package name`() {
        val result = RemoveCommand().execute(emptyList())
        assertTrue(result != 0)
    }

    @Test
    public fun `remove fails without hasab toml`() {
        val result = RemoveCommand().execute(listOf("web"))
        assertTrue(result != 0)
    }

    @Test
    public fun `remove fails for non-existent dependency`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_remove2_${System.nanoTime()}")
        dir.mkdirs()
        val tomlFile = File(dir, "hasab.toml")
        tomlFile.writeText("[package]\nname = \"test\"\n", Charsets.UTF_8)
        try {
            val result = RemoveCommand().execute(listOf("nonexistent"))
            assertTrue(result != 0)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `remove command description is set`() {
        assertTrue(RemoveCommand().description.isNotEmpty())
    }
}
