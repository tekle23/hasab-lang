package hasab.runtime.filesystem

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class HsPathTest {

    @Test
    fun `separator is not empty`() {
        assertTrue(HsPath.separator.isNotEmpty())
    }

    @Test
    fun `lineSeparator is not empty`() {
        assertTrue(HsPath.lineSeparator.isNotEmpty())
    }

    @Test
    fun `normalize removes redundant separators`() {
        val result = HsPath.normalize("foo//bar/baz")
        assertTrue(result.contains("foo"))
        assertTrue(result.contains("bar"))
        assertTrue(result.contains("baz"))
    }

    @Test
    fun `getFileName returns file name`() {
        assertEquals("test.txt", HsPath.getFileName("foo/bar/test.txt"))
    }

    @Test
    fun `getExtension returns extension`() {
        assertEquals("txt", HsPath.getExtension("foo/bar/test.txt"))
    }

    @Test
    fun `getExtension returns empty for no extension`() {
        assertEquals("", HsPath.getExtension("foo/bar/test"))
    }

    @Test
    fun `getBaseName returns name without extension`() {
        assertEquals("test", HsPath.getBaseName("foo/bar/test.txt"))
    }

    @Test
    fun `getParent returns parent directory`() {
        val parent = HsPath.getParent("/foo/bar/test.txt")
        assertNotNull(parent)
        assertTrue(parent.contains("bar"))
    }

    @Test
    fun `isAbsolute for absolute path`() {
        if (System.getProperty("os.name").lowercase().contains("win")) {
            assertTrue(HsPath.isAbsolute("C:\\test"))
        } else {
            assertTrue(HsPath.isAbsolute("/test"))
        }
    }

    @Test
    fun `isRelative for relative path`() {
        assertTrue(HsPath.isRelative("foo/bar"))
    }

    @Test
    fun `join combines path parts`() {
        val result = HsPath.join("foo", "bar", "baz.txt")
        assertTrue(result.contains("foo"))
        assertTrue(result.contains("bar"))
        assertTrue(result.contains("baz.txt"))
    }

    @Test
    fun `join with empty returns empty`() {
        assertEquals("", HsPath.join())
    }

    @Test
    fun `changeExtension modifies extension`() {
        val result = HsPath.changeExtension("test.txt", "md")
        assertTrue(result.endsWith("test.md"))
    }

    @Test
    fun `hasExtension checks correctly`() {
        assertTrue(HsPath.hasExtension("test.txt", "txt"))
        assertFalse(HsPath.hasExtension("test.txt", "md"))
    }

    @Test
    fun `roots returns non-empty list`() {
        assertTrue(HsPath.roots().isNotEmpty())
    }

    @Test
    fun `tempDir returns non-empty string`() {
        assertTrue(HsPath.tempDir().isNotEmpty())
    }

    @Test
    fun `homeDir returns non-empty string`() {
        assertTrue(HsPath.homeDir().isNotEmpty())
    }
}
