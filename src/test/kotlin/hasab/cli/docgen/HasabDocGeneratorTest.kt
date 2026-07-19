package hasab.cli.docgen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HasabDocGeneratorTest {

    @Test
    public fun `generate produces markdown header`() {
        val source = "ጻፍ(\"hello\")"
        val result = HasabDocGenerator().generate(source, "test")
        assertTrue(result.contains("# Documentation: test"))
    }

    @Test
    public fun `extractDocEntries finds functions`() {
        val source = """
            |/// Adds two numbers
            |ተግባር መደምደም(a: ዋናብር, b: ዋናብር): ዋናብር {
            |    ተመለስ a + b
            |}
        """.trimMargin()
        val entries = HasabDocGenerator().extractDocEntries(source)
        assertEquals(1, entries.size)
        assertEquals("መደምደም", entries[0].name)
        assertEquals("function", entries[0].type)
        assertEquals("Adds two numbers", entries[0].docComment)
    }

    @Test
    public fun `extractDocEntries finds let variables`() {
        val source = "let x = 5"
        val entries = HasabDocGenerator().extractDocEntries(source)
        assertEquals(1, entries.size)
        assertEquals("x", entries[0].name)
        assertEquals("variable", entries[0].type)
    }

    @Test
    public fun `extractDocEntries finds Amharic let variables`() {
        val source = "ለ x = 5"
        val entries = HasabDocGenerator().extractDocEntries(source)
        assertEquals(1, entries.size)
        assertEquals("x", entries[0].name)
    }

    @Test
    public fun `extractDocEntries ignores single-line comments`() {
        val source = """
            |// This is a comment
            |let x = 5
        """.trimMargin()
        val entries = HasabDocGenerator().extractDocEntries(source)
        assertEquals(1, entries.size)
        assertEquals("x", entries[0].name)
    }

    @Test
    public fun `extractDocEntries returns empty for empty source`() {
        val entries = HasabDocGenerator().extractDocEntries("")
        assertTrue(entries.isEmpty())
    }

    @Test
    public fun `generate includes function signature in code block`() {
        val source = """
            |/// A hello function
            |fn hello() {
            |    print("hi")
            |}
        """.trimMargin()
        val result = HasabDocGenerator().generate(source, "test")
        assertTrue(result.contains("fn hello()"))
        assertTrue(result.contains("```hasab"))
    }

    @Test
    public fun `generateForDirectory creates output file`() {
        val srcDir = File(System.getProperty("java.io.tmpdir"), "hasab_doc_src_${System.nanoTime()}")
        val outFile = File(System.getProperty("java.io.tmpdir"), "hasab_doc_out_${System.nanoTime()}.md")
        srcDir.mkdirs()
        try {
            File(srcDir, "main.has").writeText(
                "/// Entry point\nfn main() {\n    print(\"hi\")\n}\n",
                Charsets.UTF_8,
            )
            val content = HasabDocGenerator().generateForDirectory(srcDir, outFile)
            assertTrue(content.contains("HASAB Project Documentation"))
            assertTrue(outFile.exists())
        } finally {
            srcDir.deleteRecursively()
            outFile.delete()
        }
    }
}
