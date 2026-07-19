package hasab.runtime.filesystem

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class HsTempFileTest {

    @Test
    fun `create creates temp file`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            assertTrue(tmp.exists)
            assertTrue(tmp.path.isNotEmpty())
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `write and read text round trip`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            tmp.writeText("hello world")
            assertEquals("hello world", tmp.readText())
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `write and read bytes round trip`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            val data = byteArrayOf(1, 2, 3, 4, 5)
            tmp.writeBytes(data)
            assertEquals(5, tmp.readBytes().size)
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `size returns correct size`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            tmp.writeText("hello")
            assertEquals(5, tmp.size)
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `cleanup deletes file`() {
        val tmp = HsTempFile.create(prefix = "hs_test", deleteOnExit = false)
        assertTrue(tmp.exists)
        assertTrue(tmp.cleanup())
        assertFalse(tmp.exists)
    }

    @Test
    fun `close deletes file`() {
        val tmp = HsTempFile.create(prefix = "hs_test", deleteOnExit = false)
        tmp.close()
        assertFalse(tmp.exists)
    }

    @Test
    fun `toHsFile returns valid HsFile`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            val hsFile = tmp.toHsFile()
            assertTrue(hsFile.exists)
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `createDirectory creates temp directory`() {
        val tmp = HsTempFile.createDirectory(prefix = "hs_test")
        try {
            assertTrue(tmp.exists)
            assertTrue(tmp.isDirectory)
        } finally {
            tmp.cleanup()
        }
    }

    @Test
    fun `toString returns string`() {
        val tmp = HsTempFile.create(prefix = "hs_test")
        try {
            assertTrue(tmp.toString().contains("HsTempFile"))
        } finally {
            tmp.cleanup()
        }
    }
}
