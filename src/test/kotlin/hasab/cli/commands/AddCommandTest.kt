package hasab.cli.commands

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class AddCommandTest {

    @Test
    public fun `add command has correct name`() {
        assertEquals("add", AddCommand().name)
    }

    @Test
    public fun `add fails without package name`() {
        val result = AddCommand().execute(emptyList())
        assertTrue(result != 0)
    }

    @Test
    public fun `add fails without hasab toml`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_add_${System.nanoTime()}")
        dir.mkdirs()
        val origDir = System.getProperty("user.dir")
        try {
            System.setProperty("user.dir", dir.absolutePath)
            val result = AddCommand().execute(listOf("web"))
            assertTrue(result != 0)
        } finally {
            System.setProperty("user.dir", origDir)
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `add command description is set`() {
        assertTrue(AddCommand().description.isNotEmpty())
    }
}
