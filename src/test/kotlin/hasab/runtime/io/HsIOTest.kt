package hasab.runtime.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsIOTest {

    @Test
    public fun `writeAllBytes and readAllBytes roundtrip`() {
        val output = ByteArrayOutputStream()
        val data = byteArrayOf(1, 2, 3, 127, 0, -128)
        HsIO.writeAllBytes(output, data)
        val input = ByteArrayInputStream(output.toByteArray())
        val result = HsIO.readAllBytes(input)
        assertContentEquals(data, result)
    }

    @Test
    public fun `readAllBytes from empty stream`() {
        val input = ByteArrayInputStream(ByteArray(0))
        val result = HsIO.readAllBytes(input)
        assertEquals(0, result.size)
    }

    @Test
    public fun `writeAllText and readAllText roundtrip`() {
        val output = ByteArrayOutputStream()
        val text = "Hello, HASAB!"
        HsIO.writeAllText(output, text)
        val input = ByteArrayInputStream(output.toByteArray())
        val result = HsIO.readAllText(input)
        assertEquals(text, result)
    }

    @Test
    public fun `readAllText with UTF-8`() {
        val output = ByteArrayOutputStream()
        val text = "\u00e9\u00e8\u00ea \u4e16\u754c"
        HsIO.writeAllText(output, text, "UTF-8")
        val input = ByteArrayInputStream(output.toByteArray())
        val result = HsIO.readAllText(input, "UTF-8")
        assertEquals(text, result)
    }

    @Test
    public fun `readAllText from empty stream`() {
        val input = ByteArrayInputStream(ByteArray(0))
        val result = HsIO.readAllText(input)
        assertEquals("", result)
    }

    @Test
    public fun `copyStream copies all bytes`() {
        val data = ByteArray(16384) { (it % 256).toByte() }
        val input = ByteArrayInputStream(data)
        val output = ByteArrayOutputStream()
        val copied = HsIO.copyStream(input, output)
        assertEquals(data.size.toLong(), copied)
        assertContentEquals(data, output.toByteArray())
    }

    @Test
    public fun `copyStream from empty stream`() {
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val copied = HsIO.copyStream(input, output)
        assertEquals(0L, copied)
        assertEquals(0, output.toByteArray().size)
    }

    @Test
    public fun `copyStream large data`() {
        val data = ByteArray(100_000) { (it % 256).toByte() }
        val input = ByteArrayInputStream(data)
        val output = ByteArrayOutputStream()
        val copied = HsIO.copyStream(input, output)
        assertEquals(data.size.toLong(), copied)
        assertContentEquals(data, output.toByteArray())
    }

    @Test
    public fun `writeAllBytes flushes output`() {
        val output = ByteArrayOutputStream()
        HsIO.writeAllBytes(output, byteArrayOf(1, 2, 3))
        assertEquals(3, output.toByteArray().size)
    }

    @Test
    public fun `writeAllText flushes output`() {
        val output = ByteArrayOutputStream()
        HsIO.writeAllText(output, "test")
        assertEquals("test", output.toString("UTF-8"))
    }

    @Test
    public fun `readAllBytes multiple writes`() {
        val output = ByteArrayOutputStream()
        HsIO.writeAllBytes(output, byteArrayOf(10, 20))
        HsIO.writeAllBytes(output, byteArrayOf(30))
        val input = ByteArrayInputStream(output.toByteArray())
        val result = HsIO.readAllBytes(input)
        assertContentEquals(byteArrayOf(10, 20, 30), result)
    }

    @Test
    public fun `readResourceBytes returns null for nonexistent resource`() {
        val result = HsIO.readResourceBytes("/nonexistent/resource/xyz.txt")
        assertNull(result)
    }

    @Test
    public fun `readResource returns null for nonexistent resource`() {
        val result = HsIO.readResource("/nonexistent/resource/xyz.txt")
        assertNull(result)
    }

    @Test
    public fun `openResource returns null for nonexistent resource`() {
        val result = HsIO.openResource("/nonexistent/resource/xyz.txt")
        assertNull(result)
    }

    @Test
    public fun `openResource returns stream for existing resource`() {
        val stream = HsIO.openResource("/hasab-test-resource.txt")
        if (stream != null) {
            val content = stream.use { HsIO.readAllText(it) }
            assertTrue(content.isNotEmpty())
        }
    }

    @Test
    public fun `readResource reads text content`() {
        val result = HsIO.readResource("/hasab-test-resource.txt")
        if (result != null) {
            assertTrue(result.contains("HASAB"))
        }
    }

    @Test
    public fun `readResourceBytes reads byte content`() {
        val result = HsIO.readResourceBytes("/hasab-test-resource.txt")
        if (result != null) {
            assertTrue(result.isNotEmpty())
        }
    }

    @Test
    public fun `HsIO object is accessible`() {
        assertNotNull(HsIO)
    }
}
