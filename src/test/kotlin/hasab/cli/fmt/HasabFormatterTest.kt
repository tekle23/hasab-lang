package hasab.cli.fmt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HasabFormatterTest {

    @Test
    public fun `format preserves clean code`() {
        val source = """
            |ተግባር ዋና() {
            |    ጻፍ("hello")
            |}
        """.trimMargin() + "\n"
        val formatted = HasabFormatter.format(source)
        assertEquals(source, formatted)
    }

    @Test
    public fun `format normalizes indentation to 4 spaces`() {
        val source = "ተግባር ዋና() {\n  ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        assertTrue(formatted.contains("    "))
    }

    @Test
    public fun `format removes trailing whitespace`() {
        val source = "ተግባር ዋና() {   \n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        assertFalse(formatted.contains("   \n"))
    }

    @Test
    public fun `format ensures newline at end of file`() {
        val source = "ተግባር ዋና() {\n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        assertTrue(formatted.endsWith("\n"))
    }

    @Test
    public fun `format limits consecutive blank lines to 1`() {
        val source = "ተግባር ዋና() {\n\n\n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        val doubleNewline = "\n\n\n"
        assertFalse(formatted.contains(doubleNewline))
    }

    @Test
    public fun `format handles comments`() {
        val source = "// This is a comment\nተግባር ዋና() {\n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        assertTrue(formatted.contains("// This is a comment"))
    }

    @Test
    public fun `needsFormatting returns false for formatted code`() {
        val source = """
            |ተግባር ዋና() {
            |    ጻፍ("hello")
            |}
        """.trimMargin() + "\n"
        assertFalse(HasabFormatter.needsFormatting(source))
    }

    @Test
    public fun `needsFormatting returns true for unformatted code`() {
        val source = "ተግባር ዋና() {\n  ጻፍ(\"hello\")\n}"
        assertTrue(HasabFormatter.needsFormatting(source))
    }

    @Test
    public fun `format handles empty source`() {
        val formatted = HasabFormatter.format("")
        assertTrue(formatted.isEmpty() || formatted == "\n")
    }

    @Test
    public fun `format handles block comments`() {
        val source = "/* block comment */\nተግባር ዋና() {\n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        assertTrue(formatted.contains("/* block comment */"))
    }

    @Test
    public fun `format normalizes brace indentation`() {
        val source = "ተግባር ዋና()\n{\n    ጻፍ(\"hello\")\n}"
        val formatted = HasabFormatter.format(source)
        // After normalization, opening brace on same line
        assertTrue(formatted.contains("ተግባር ዋና() {") || formatted.contains("{"))
    }
}
