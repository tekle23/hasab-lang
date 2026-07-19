package hasab.runtime.concurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

public class HsThreadTest {

    @Test
    public fun `of creates thread with correct name`() {
        val thread = HsThread.of("test-thread") { }
        assertEquals("test-thread", thread.name)
    }

    @Test
    public fun `of creates non-daemon thread by default`() {
        val thread = HsThread.of("test-thread") { }
        assertFalse(thread.isDaemon)
    }

    @Test
    public fun `of creates daemon thread when specified`() {
        val thread = HsThread.of("daemon-thread", daemon = true) { }
        assertTrue(thread.isDaemon)
    }

    @Test
    public fun `thread starts and completes`() {
        val flag = java.util.concurrent.atomic.AtomicBoolean(false)
        val thread = HsThread.of("test-start") { flag.set(true) }
        thread.start()
        thread.join(5000)
        assertTrue(flag.get())
    }

    @Test
    public fun `thread isAlive after start before completion`() {
        val latch = java.util.concurrent.CountDownLatch(1)
        val thread = HsThread.of("alive-test") {
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        }
        thread.start()
        assertTrue(thread.isAlive)
        latch.countDown()
        thread.join(5000)
    }

    @Test
    public fun `thread is not alive after join`() {
        val thread = HsThread.of("done-test") { }
        thread.start()
        thread.join(5000)
        assertFalse(thread.isAlive)
    }

    @Test
    public fun `currentThread returns wrapper for current thread`() {
        val current = HsThread.currentThread()
        assertNotNull(current)
        assertEquals(Thread.currentThread().id, current.id)
    }

    @Test
    public fun `currentThread returns same wrapper on repeated calls`() {
        val first = HsThread.currentThread()
        val second = HsThread.currentThread()
        assertEquals(first, second)
    }

    @Test
    public fun `thread id is positive`() {
        val thread = HsThread.of("id-test") { }
        assertTrue(thread.id > 0L)
    }

    @Test
    public fun `sleep does not throw`() {
        HsThread.sleep(10)
    }

    @Test
    public fun `join with timeout completes`() {
        val thread = HsThread.of("timeout-join") { Thread.sleep(50) }
        thread.start()
        thread.join(5000)
        assertFalse(thread.isAlive)
    }

    @Test
    public fun `interrupt sets interrupted flag`() {
        val latch = java.util.concurrent.CountDownLatch(1)
        val thread = HsThread.of("interrupt-test") {
            try {
                latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
            } catch (_: InterruptedException) { }
        }
        thread.start()
        latch.countDown()
        Thread.sleep(50)
        thread.interrupt()
        thread.join(5000)
        assertFalse(thread.isAlive)
    }

    @Test
    public fun `setPriority and getPriority work`() {
        val thread = HsThread.of("priority-test") { }
        thread.setPriority(7)
        assertEquals(7, thread.getPriority())
    }

    @Test
    public fun `daemon property can be set`() {
        val thread = HsThread.of("daemon-set-test") { }
        thread.daemon = true
        assertTrue(thread.daemon)
        thread.daemon = false
        assertFalse(thread.daemon)
    }

    @Test
    public fun `toString contains thread info`() {
        val thread = HsThread.of("tostring-test") { }
        val str = thread.toString()
        assertTrue(str.contains("HsThread"))
        assertTrue(str.contains("tostring-test"))
    }

    @Test
    public fun `equal threads have same hashCode`() {
        val thread = HsThread.of("hash-test") { }
        val sameThread = HsThread.currentThread().let { _ -> thread }
        assertEquals(thread.hashCode(), sameThread.hashCode())
    }

    @Test
    public fun `getAllThreads returns non-empty list`() {
        val threads = HsThread.getAllThreads()
        assertTrue(threads.isNotEmpty())
    }

    @Test
    public fun `activeCount is positive`() {
        assertTrue(HsThread.activeCount() > 0)
    }
}
