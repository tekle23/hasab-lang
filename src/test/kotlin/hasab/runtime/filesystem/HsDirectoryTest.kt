package hasab.runtime.filesystem

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class HsDirectoryTest {

    private lateinit var tempDir: File

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "hs_dir_test_${System.nanoTime()}")
        tempDir.mkdirs()
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `exists returns true for existing directory`() {
        val dir = HsDirectory(tempDir.absolutePath)
        assertTrue(dir.exists)
    }

    @Test
    fun `exists returns false for non-existent directory`() {
        val dir = HsDirectory(File(tempDir, "noexist").absolutePath)
        assertFalse(dir.exists)
    }

    @Test
    fun `isEmpty returns true for empty directory`() {
        val dir = HsDirectory(tempDir.absolutePath)
        assertTrue(dir.isEmpty)
    }

    @Test
    fun `isEmpty returns false for non-empty directory`() {
        File(tempDir, "file.txt").createNewFile()
        val dir = HsDirectory(tempDir.absolutePath)
        assertFalse(dir.isEmpty)
    }

    @Test
    fun `create creates directory`() {
        val target = File(tempDir, "subdir")
        val dir = HsDirectory(target.absolutePath)
        assertTrue(dir.create())
        assertTrue(target.exists() && target.isDirectory)
    }

    @Test
    fun `listFiles returns files`() {
        File(tempDir, "a.txt").createNewFile()
        File(tempDir, "b.txt").createNewFile()
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(2, dir.listFiles().size)
    }

    @Test
    fun `listFilesOnly returns only files`() {
        File(tempDir, "a.txt").createNewFile()
        File(tempDir, "subdir").mkdirs()
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(1, dir.listFilesOnly().size)
    }

    @Test
    fun `listDirectories returns only directories`() {
        File(tempDir, "a.txt").createNewFile()
        File(tempDir, "subdir").mkdirs()
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(1, dir.listDirectories().size)
    }

    @Test
    fun `createFile creates file`() {
        val dir = HsDirectory(tempDir.absolutePath)
        val file = dir.createFile("new.txt")
        assertTrue(file.exists)
        assertEquals("new.txt", file.name)
    }

    @Test
    fun `createSubdirectory creates subdir`() {
        val dir = HsDirectory(tempDir.absolutePath)
        val sub = dir.createSubdirectory("child")
        assertTrue(sub.exists)
    }

    @Test
    fun `deleteRecursively removes all`() {
        File(tempDir, "child").mkdirs()
        File(tempDir, "child/file.txt").createNewFile()
        val dir = HsDirectory(tempDir.absolutePath)
        assertTrue(dir.deleteRecursively())
        assertFalse(tempDir.exists())
    }

    @Test
    fun `copyTo copies directory`() {
        File(tempDir, "file.txt").writeText("hello")
        val src = HsDirectory(tempDir.absolutePath)
        val destParent = File(System.getProperty("java.io.tmpdir"), "hs_copy_dest_${System.nanoTime()}")
        destParent.mkdirs()
        try {
            val destDir = File(destParent, "dest")
            val dest = HsDirectory(destDir.absolutePath)
            assertTrue(src.copyTo(dest))
            assertTrue(destDir.exists())
        } finally {
            destParent.deleteRecursively()
        }
    }

    @Test
    fun `tempDir creates temp directory`() {
        val dir = HsDirectory.tempDir("hs_test")
        assertTrue(dir.exists)
        dir.deleteRecursively()
    }

    @Test
    fun `walkFiles finds all files recursively`() {
        File(tempDir, "a.txt").createNewFile()
        File(tempDir, "sub").mkdirs()
        File(tempDir, "sub/b.txt").createNewFile()
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(2, dir.walkFiles().size)
    }

    @Test
    fun `path property returns correct path`() {
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(tempDir.absolutePath, dir.path)
    }

    @Test
    fun `childCount returns correct count`() {
        File(tempDir, "a.txt").createNewFile()
        File(tempDir, "b.txt").createNewFile()
        val dir = HsDirectory(tempDir.absolutePath)
        assertEquals(2, dir.childCount)
    }

    @Test
    fun `equals and hashCode work`() {
        val dir1 = HsDirectory(tempDir.absolutePath)
        val dir2 = HsDirectory(tempDir.absolutePath)
        assertEquals(dir1, dir2)
        assertEquals(dir1.hashCode(), dir2.hashCode())
    }

    @Test
    fun `fromPath creates directory`() {
        val dir = HsDirectory.fromPath(tempDir.absolutePath)
        assertTrue(dir.exists)
    }
}
