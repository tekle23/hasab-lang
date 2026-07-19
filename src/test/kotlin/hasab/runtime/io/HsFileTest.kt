package hasab.runtime.io

import hasab.runtime.exceptions.HsIOError
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.test.assertContentEquals

public class HsFileTest {

    private fun tempDir(): HsFile {
        val dir = HsFile.createTempDirectory("hs-test")
        return dir
    }

    private fun tempFile(suffix: String = ".txt"): HsFile {
        val f = HsFile.tempFile(prefix = "hs-test", suffix = suffix)
        return f
    }

    private fun deleteRecursive(f: File) {
        if (f.isDirectory) {
            f.listFiles()?.forEach { deleteRecursive(it) }
        }
        f.delete()
    }

    @Test
    public fun `creation from path`() {
        val f = HsFile(File("/tmp/test.txt").path)
        assertEquals(File("/tmp/test.txt").path, f.path)
    }

    @Test
    public fun `fromPath factory`() {
        val f = HsFile.fromPath(File("/tmp/test.txt").path)
        assertEquals(File("/tmp/test.txt").path, f.path)
    }

    @Test
    public fun `name property`() {
        val f = HsFile("/tmp/somefile.dat")
        assertEquals("somefile.dat", f.name)
    }

    @Test
    public fun `extension property`() {
        val f = HsFile("/tmp/archive.tar.gz")
        assertEquals("gz", f.extension)
    }

    @Test
    public fun `extension is empty when none`() {
        val f = HsFile("/tmp/Makefile")
        assertEquals("", f.extension)
    }

    @Test
    public fun `parentPath property`() {
        val f = HsFile("/tmp/sub/file.txt")
        assertEquals(File("/tmp/sub").absolutePath, File(f.parentPath!!).absolutePath)
    }

    @Test
    public fun `parentPath is null for root-level name`() {
        val f = HsFile("file.txt")
        assertNull(f.parentPath)
    }

    @Test
    public fun `absolutePath is not null`() {
        val f = HsFile("relative/path.txt")
        assertTrue(f.absolutePath.isNotEmpty())
    }

    @Test
    public fun `exists is false for nonexistent file`() {
        val f = HsFile("/tmp/hasab_nonexistent_12345.txt")
        assertFalse(f.exists)
    }

    @Test
    public fun `exists is true for temp file`() {
        val f = tempFile()
        assertTrue(f.exists)
        f.delete()
    }

    @Test
    public fun `isFile is true for temp file`() {
        val f = tempFile()
        assertTrue(f.isFile)
        f.delete()
    }

    @Test
    public fun `isDirectory is true for temp dir`() {
        val d = tempDir()
        assertTrue(d.isDirectory)
        deleteRecursive(File(d.path))
    }

    @Test
    public fun `isFile is false for directory`() {
        val d = tempDir()
        assertFalse(d.isFile)
        deleteRecursive(File(d.path))
    }

    @Test
    public fun `size is zero for empty file`() {
        val f = tempFile()
        assertEquals(0L, f.size)
        f.delete()
    }

    @Test
    public fun `size reflects content length`() {
        val f = tempFile()
        f.writeAllText("hello")
        assertEquals(5L, f.size)
        f.delete()
    }

    @Test
    public fun `writeAllText and readAllText roundtrip`() {
        val f = tempFile()
        f.writeAllText("hello world")
        assertEquals("hello world", f.readAllText())
        f.delete()
    }

    @Test
    public fun `writeAllText overwrites previous content`() {
        val f = tempFile()
        f.writeAllText("first")
        f.writeAllText("second")
        assertEquals("second", f.readAllText())
        f.delete()
    }

    @Test
    public fun `writeAllBytes and readAllBytes roundtrip`() {
        val f = tempFile()
        val data = byteArrayOf(0x01, 0x02, 0xFF.toByte(), 0x80.toByte())
        f.writeAllBytes(data)
        assertContentEquals(data, f.readAllBytes())
        f.delete()
    }

    @Test
    public fun `readAllLines`() {
        val f = tempFile()
        f.writeAllText("line1\nline2\nline3")
        val lines = f.readAllLines()
        assertEquals(3, lines.size)
        assertEquals("line1", lines[0])
        assertEquals("line2", lines[1])
        assertEquals("line3", lines[2])
        f.delete()
    }

    @Test
    public fun `readLines is alias for readAllLines`() {
        val f = tempFile()
        f.writeAllText("a\nb")
        assertEquals(f.readAllLines(), f.readLines())
        f.delete()
    }

    @Test
    public fun `appendText adds to end`() {
        val f = tempFile()
        f.writeAllText("hello")
        f.appendText(" world")
        assertEquals("hello world", f.readAllText())
        f.delete()
    }

    @Test
    public fun `appendLine adds with newline`() {
        val f = tempFile()
        f.writeAllText("first")
        f.appendLine("second")
        val content = f.readAllText()
        assertTrue(content.startsWith("first"))
        assertTrue(content.contains("second"))
        f.delete()
    }

    @Test
    public fun `delete returns true for existing file`() {
        val f = tempFile()
        assertTrue(f.delete())
        assertFalse(f.exists)
    }

    @Test
    public fun `delete returns false for nonexistent file`() {
        val f = HsFile("/tmp/hasab_nonexistent_delete_test.txt")
        assertFalse(f.delete())
    }

    @Test
    public fun `createNewFile returns true when created`() {
        val f = tempFile()
        f.delete()
        assertTrue(f.createNewFile())
        assertTrue(f.exists)
        f.delete()
    }

    @Test
    public fun `createNewFile returns false when already exists`() {
        val f = tempFile()
        assertFalse(f.createNewFile())
        f.delete()
    }

    @Test
    public fun `mkdir creates directory`() {
        val parent = tempDir()
        val child = parent.resolve("newdir")
        assertTrue(child.mkdir())
        assertTrue(child.isDirectory)
        deleteRecursive(File(parent.path))
    }

    @Test
    public fun `mkdirs creates nested directories`() {
        val parent = tempDir()
        val nested = parent.resolve("a").resolve("b").resolve("c")
        assertTrue(nested.mkdirs())
        assertTrue(nested.isDirectory)
        deleteRecursive(File(parent.path))
    }

    @Test
    public fun `listFiles returns children`() {
        val dir = tempDir()
        dir.resolve("a.txt").writeAllText("a")
        dir.resolve("b.txt").writeAllText("b")
        val files = dir.listFiles()
        assertEquals(2, files.size)
        val names = files.map { it.name }.toSet()
        assertTrue("a.txt" in names)
        assertTrue("b.txt" in names)
        deleteRecursive(File(dir.path))
    }

    @Test
    public fun `listFiles returns empty for non-directory`() {
        val f = tempFile()
        val files = f.listFiles()
        assertEquals(0, files.size)
        f.delete()
    }

    @Test
    public fun `listFilesFiltered by extension`() {
        val dir = tempDir()
        dir.resolve("a.txt").writeAllText("a")
        dir.resolve("b.rs").writeAllText("b")
        dir.resolve("c.txt").writeAllText("c")
        val txtFiles = dir.listFilesFiltered("txt")
        assertEquals(2, txtFiles.size)
        assertTrue(txtFiles.all { it.extension == "txt" })
        deleteRecursive(File(dir.path))
    }

    @Test
    public fun `listFilesFiltered returns empty when no match`() {
        val dir = tempDir()
        dir.resolve("a.txt").writeAllText("a")
        val rsFiles = dir.listFilesFiltered("rs")
        assertEquals(0, rsFiles.size)
        deleteRecursive(File(dir.path))
    }

    @Test
    public fun `resolve creates child file`() {
        val dir = tempDir()
        val child = dir.resolve("child.txt")
        assertEquals("child.txt", child.name)
        assertTrue(child.absolutePath.contains(dir.path))
        deleteRecursive(File(dir.path))
    }

    @Test
    public fun `relativeTo computes relative path`() {
        val dir = tempDir()
        val child = dir.resolve("sub")
        assertTrue(child.relativeTo(dir).contains("sub"))
        deleteRecursive(File(dir.path))
    }

    @Test
    public fun `forEachLine iterates lines`() {
        val f = tempFile()
        f.writeAllText("line1\nline2\nline3")
        val collected = mutableListOf<String>()
        f.forEachLine { collected.add(it) }
        assertEquals(listOf("line1", "line2", "line3"), collected)
        f.delete()
    }

    @Test
    public fun `copyTo copies file`() {
        val src = tempFile()
        src.writeAllText("copy me")
        val dst = tempFile()
        assertTrue(src.copyTo(dst))
        assertEquals("copy me", dst.readAllText())
        src.delete()
        dst.delete()
    }

    @Test
    public fun `moveTo moves file`() {
        val src = tempFile()
        src.writeAllText("move me")
        val dstFile = java.io.File.createTempFile("hs-test-move-dst", ".txt")
        dstFile.delete()
        val dst = HsFile(dstFile.absolutePath)
        assertTrue(src.moveTo(dst))
        assertFalse(src.exists)
        assertEquals("move me", dst.readAllText())
        dst.delete()
    }

    @Test
    public fun `tempFile factory creates file`() {
        val f = HsFile.tempFile(prefix = "hs-test-tmp", suffix = ".txt")
        assertTrue(f.exists)
        assertTrue(f.isFile)
        f.delete()
    }

    @Test
    public fun `tempFile with custom prefix and suffix`() {
        val f = HsFile.tempFile(prefix = "custom", suffix = ".dat")
        assertTrue(f.name.startsWith("custom"))
        assertTrue(f.name.endsWith(".dat"))
        f.delete()
    }

    @Test
    public fun `createTempDirectory factory`() {
        val d = HsFile.createTempDirectory("hs-test-dir")
        assertTrue(d.exists)
        assertTrue(d.isDirectory)
        deleteRecursive(File(d.path))
    }

    @Test
    public fun `equals and hashCode based on absolute path`() {
        val f1 = HsFile(tempFile().absolutePath)
        val f2 = HsFile(f1.absolutePath)
        assertEquals(f1, f2)
        assertEquals(f1.hashCode(), f2.hashCode())
        f1.delete()
    }

    @Test
    public fun `equals returns false for different paths`() {
        val f1 = HsFile("/tmp/a.txt")
        val f2 = HsFile("/tmp/b.txt")
        assertFalse(f1 == f2)
    }

    @Test
    public fun `equals returns false for non-HsFile`() {
        val f = HsFile("/tmp/a.txt")
        assertFalse(f.equals("not a file"))
    }

    @Test
    public fun `toString formats as HsFile`() {
        val f = HsFile(File("/tmp/test.txt").path)
        assertEquals("HsFile(${File("/tmp/test.txt").path})", f.toString())
    }

    @Test
    public fun `readAllText on nonexistent file throws HsIOError`() {
        val f = HsFile("/tmp/hasab_nonexistent_read_test.txt")
        assertFailsWith<HsIOError> { f.readAllText() }
    }

    @Test
    public fun `readAllBytes on nonexistent file throws HsIOError`() {
        val f = HsFile("/tmp/hasab_nonexistent_read_bytes_test.txt")
        assertFailsWith<HsIOError> { f.readAllBytes() }
    }

    @Test
    public fun `lastModified is positive for existing file`() {
        val f = tempFile()
        assertTrue(f.lastModified > 0)
        f.delete()
    }

    @Test
    public fun `isReadable for temp file`() {
        val f = tempFile()
        assertTrue(f.isReadable)
        f.delete()
    }

    @Test
    public fun `isWritable for temp file`() {
        val f = tempFile()
        assertTrue(f.isWritable)
        f.delete()
    }
}
