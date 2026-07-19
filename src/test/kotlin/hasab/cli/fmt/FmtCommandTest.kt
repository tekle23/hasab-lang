package hasab.cli.fmt

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class FmtCommandTest {

    @Test
    public fun `fmt command has correct name`() {
        val cmd = FmtCommand()
        assertEquals("fmt", cmd.name)
    }

    @Test
    public fun `fmt on empty project prints no files message`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_fmt_empty_${System.nanoTime()}")
        dir.mkdirs()
        try {
            val source = "ተግባር ዋና() {\n    ጻፍ(\"hello\")\n}\n"
            assertTrue(!HasabFormatter.needsFormatting(source))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `formatFile returns false when already formatted`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_fmt_file_${System.nanoTime()}")
        dir.mkdirs()
        val file = File(dir, "test.has")
        file.writeText("ተግባር ዋና() {\n    ጻፍ(\"hello\")\n}\n", Charsets.UTF_8)
        try {
            val changed = HasabFormatter.formatFile(file)
            assertFalse(changed)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `formatFile returns true and fixes unformatted file`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_fmt_fix_${System.nanoTime()}")
        dir.mkdirs()
        val file = File(dir, "test.has")
        file.writeText("ተግባር ዋና() {\n  ጻፍ(\"hello\")\n}", Charsets.UTF_8)
        try {
            val changed = HasabFormatter.formatFile(file)
            assertTrue(changed)
            val content = file.readText(Charsets.UTF_8)
            assertTrue(content.contains("    ጻፍ"))
        } finally {
            dir.deleteRecursively()
        }
    }
}
