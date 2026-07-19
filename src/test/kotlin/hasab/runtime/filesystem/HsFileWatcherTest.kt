package hasab.runtime.filesystem

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class HsFileWatcherTest {

    @Test
    fun `watcher starts and stops`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "hs_watcher_test_${System.nanoTime()}")
        tempDir.mkdirs()
        try {
            val watcher = HsFileWatcher(tempDir.absolutePath, intervalMs = 50)
            assertFalse(watcher.isRunning())
            watcher.start()
            assertTrue(watcher.isRunning())
            Thread.sleep(100)
            watcher.stop()
            assertFalse(watcher.isRunning())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `watcher detects new file`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "hs_watcher_test_${System.nanoTime()}")
        tempDir.mkdirs()
        try {
            val events = mutableListOf<HsWatchEvent>()
            val watcher = HsFileWatcher(tempDir.absolutePath, intervalMs = 50)
            watcher.onEvent { events.add(it) }
            watcher.start()
            Thread.sleep(150)
            File(tempDir, "new.txt").writeText("hello")
            Thread.sleep(300)
            watcher.stop()
            assertTrue(events.any { it.kind == HsWatchEvent.Kind.CREATED })
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `watcher detects deleted file`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "hs_watcher_test_${System.nanoTime()}")
        tempDir.mkdirs()
        try {
            val events = mutableListOf<HsWatchEvent>()
            val file = File(tempDir, "to_delete.txt")
            file.writeText("bye")
            val watcher = HsFileWatcher(tempDir.absolutePath, intervalMs = 50)
            watcher.onEvent { events.add(it) }
            watcher.start()
            Thread.sleep(150)
            file.delete()
            Thread.sleep(300)
            watcher.stop()
            assertTrue(events.any { it.kind == HsWatchEvent.Kind.DELETED })
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `HsWatchEvent toString works`() {
        val event = HsWatchEvent("/tmp/test", HsWatchEvent.Kind.CREATED)
        assertTrue(event.toString().contains("CREATED"))
    }
}
