package hasab.cli.config

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

public class HasabTomlTest {

    @Test
    public fun `parse returns empty map for empty content`() {
        val result = HasabToml.parse("")
        assertTrue(result.isEmpty())
    }

    @Test
    public fun `parse extracts key-value pairs`() {
        val content = """
            [package]
            name = "myapp"
            version = "1.0.0"
        """.trimIndent()
        val result = HasabToml.parse(content)
        assertEquals("myapp", result["package.name"])
        assertEquals("1.0.0", result["package.version"])
    }

    @Test
    public fun `parse handles nested sections`() {
        val content = """
            [package]
            name = "myapp"

            [dependencies]
            web = "1.0"
            json = "2.1"
        """.trimIndent()
        val result = HasabToml.parse(content)
        assertEquals("myapp", result["package.name"])
        assertEquals("1.0", result["dependencies.web"])
        assertEquals("2.1", result["dependencies.json"])
    }

    @Test
    public fun `parse handles comments`() {
        val content = """
            # This is a comment
            [package]
            name = "myapp" # inline comment
        """.trimIndent()
        val result = HasabToml.parse(content)
        assertEquals("myapp", result["package.name"])
    }

    @Test
    public fun `parse handles quoted strings`() {
        val content = """
            [package]
            name = "hello world"
            description = "A project"
        """.trimIndent()
        val result = HasabToml.parse(content)
        assertEquals("hello world", result["package.name"])
        assertEquals("A project", result["package.description"])
    }

    @Test
    public fun `serialize produces valid TOML`() {
        val data = mapOf(
            "package.name" to "test",
            "package.version" to "0.1.0",
            "dependencies.web" to "1.0",
        )
        val output = HasabToml.serialize(data)
        assertTrue(output.contains("test"))
        assertTrue(output.contains("0.1.0"))
        assertTrue(output.contains("web"))
        assertTrue(output.contains("1.0"))
    }

    @Test
    public fun `parse and serialize roundtrip`() {
        val content = """
            [package]
            name = "roundtrip"
            version = "2.0"
        """.trimIndent()
        val parsed = HasabToml.parse(content)
        val serialized = HasabToml.serialize(parsed)
        assertTrue(serialized.contains("roundtrip"))
        assertTrue(serialized.contains("2.0"))
    }

    @Test
    public fun `parse handles empty file`() {
        val result = HasabToml.parse(File("nonexistent.toml"))
        // Should return empty map or throw gracefully
    }

    @Test
    public fun `parse handles standalone values without section`() {
        val content = """
            name = "standalone"
        """.trimIndent()
        val result = HasabToml.parse(content)
        assertEquals("standalone", result["name"])
    }
}
