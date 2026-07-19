package hasab.runtime.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsExecutorTest {

    @Test
    public fun `fixedThreadPool creates executor`() {
        val executor = HsExecutor.fixedThreadPool(2)
        assertFalse(executor.isShutdown)
    }

    @Test
    public fun `singleThread creates executor`() {
        val executor = HsExecutor.singleThread()
        assertFalse(executor.isShutdown)
        executor.shutdown()
    }

    @Test
    public fun `cachedThreadPool creates executor`() {
        val executor = HsExecutor.cachedThreadPool()
        assertFalse(executor.isShutdown)
        executor.shutdown()
    }

    @Test
    public fun `submit returns result`() {
        val executor = HsExecutor.fixedThreadPool(1)
        try {
            val future = executor.submit { 42 }
            assertEquals(42, future.get())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `submit executes task asynchronously`() {
        val executor = HsExecutor.fixedThreadPool(1)
        try {
            val flag = AtomicBoolean(false)
            val future = executor.submit { flag.set(true); "done" }
            future.get()
            assertTrue(flag.get())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `submitAll returns multiple futures`() {
        val executor = HsExecutor.fixedThreadPool(2)
        try {
            val tasks = listOf(
                { 1 },
                { 2 },
                { 3 }
            )
            val futures = executor.submitAll(tasks)
            assertEquals(3, futures.size)
            assertEquals(1, futures[0].get())
            assertEquals(2, futures[1].get())
            assertEquals(3, futures[2].get())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `shutdown sets isShutdown`() {
        val executor = HsExecutor.fixedThreadPool(1)
        assertFalse(executor.isShutdown)
        executor.shutdown()
        assertTrue(executor.isShutdown)
    }

    @Test
    public fun `shutdownNow sets isShutdown`() {
        val executor = HsExecutor.fixedThreadPool(1)
        executor.shutdownNow()
        assertTrue(executor.isShutdown)
    }

    @Test
    public fun `awaitTermination returns true after shutdown completes`() {
        val executor = HsExecutor.fixedThreadPool(1)
        executor.submit { Thread.sleep(10) }.get()
        executor.shutdown()
        assertTrue(executor.awaitTermination(5000))
    }

    @Test
    public fun `executor handles multiple concurrent tasks`() {
        val executor = HsExecutor.fixedThreadPool(4)
        try {
            val counter = AtomicInteger(0)
            val latch = CountDownLatch(10)
            repeat(10) {
                executor.submit {
                    counter.incrementAndGet()
                    latch.countDown()
                    "done"
                }
            }
            latch.await(10, TimeUnit.SECONDS)
            assertEquals(10, counter.get())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `default constructor uses available processors`() {
        val executor = HsExecutor()
        assertFalse(executor.isShutdown)
        executor.shutdown()
    }

    @Test
    public fun `virtualThreads creates executor`() {
        val executor = HsExecutor.virtualThreads()
        assertFalse(executor.isShutdown)
        val future = executor.submit { "ok" }
        assertEquals("ok", future.get())
        executor.shutdown()
    }

    @Test
    public fun `submit returns string result`() {
        val executor = HsExecutor.fixedThreadPool(1)
        try {
            val future = executor.submit { "hello" }
            assertEquals("hello", future.get())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `submitAll with empty list returns empty list`() {
        val executor = HsExecutor.fixedThreadPool(1)
        try {
            val futures = executor.submitAll(emptyList())
            assertTrue(futures.isEmpty())
        } finally {
            executor.shutdown()
        }
    }

    @Test
    public fun `isTerminated is true after all tasks complete`() {
        val executor = HsExecutor.fixedThreadPool(1)
        executor.submit { "done" }.get()
        executor.shutdown()
        assertTrue(executor.awaitTermination(5000))
        assertTrue(executor.isTerminated)
    }
}
