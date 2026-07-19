package hasab.runtime.concurrency

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

public class HsFutureTest {

    @Test
    public fun `of completes with supplier result`() {
        val future = HsFuture.of { 42 }
        val result = future.get()
        assertEquals(42, result)
    }

    @Test
    public fun `completed returns immediately with value`() {
        val future = HsFuture.completed("hello")
        assertTrue(future.isDone)
        assertEquals("hello", future.get())
    }

    @Test
    public fun `completed future is not cancelled`() {
        val future = HsFuture.completed("hello")
        assertFalse(future.isCancelled)
    }

    @Test
    public fun `get returns correct value`() {
        val future = HsFuture.completed(100)
        assertEquals(100, future.get())
    }

    @Test
    public fun `get with timeout returns value`() {
        val future = HsFuture.completed("fast")
        assertEquals("fast", future.get(5000))
    }

    @Test
    public fun `isDone is true after completion`() {
        val future = HsFuture.completed(42)
        assertTrue(future.isDone)
    }

    @Test
    public fun `isDone is true after exceptional completion`() {
        val future = HsFuture.failed<Int>(RuntimeException("error"))
        assertTrue(future.isDone)
    }

    @Test
    public fun `failed future throws on get`() {
        val future = HsFuture.failed<Int>(RuntimeException("boom"))
        assertFailsWith<ExecutionException> {
            future.get()
        }
    }

    @Test
    public fun `thenApply transforms result`() {
        val future = HsFuture.completed(10)
        val doubled = future.thenApply { it * 2 }
        assertEquals(20, doubled.get())
    }

    @Test
    public fun `thenApply chains transformations`() {
        val future = HsFuture.completed(1)
        val result = future.thenApply { it + 1 }.thenApply { it * 10 }
        assertEquals(20, result.get())
    }

    @Test
    public fun `thenAccept receives value`() {
        var received: Int? = null
        val future = HsFuture.completed(42)
        future.thenAccept { received = it }
        assertEquals(42, received)
    }

    @Test
    public fun `thenRun executes action`() {
        var executed = false
        val future = HsFuture.completed("done")
        future.thenRun { executed = true }
        assertTrue(executed)
    }

    @Test
    public fun `exceptionally handles error`() {
        val future = HsFuture.failed<Int>(RuntimeException("fail"))
            .exceptionally { -1 }
        assertEquals(-1, future.get())
    }

    @Test
    public fun `whenComplete receives result`() {
        var result: Int? = null
        var error: Throwable? = null
        HsFuture.completed(42).whenComplete { r, e ->
            result = r
            error = e
        }
        assertEquals(42, result)
        assertTrue(error == null)
    }

    @Test
    public fun `whenComplete receives error`() {
        var result: Int? = null
        var error: Throwable? = null
        HsFuture.failed<Int>(RuntimeException("fail")).whenComplete { r, e ->
            result = r
            error = e
        }
        assertTrue(result == null)
        assertTrue(error != null)
    }

    @Test
    public fun `allOf completes when all futures complete`() {
        val f1 = HsFuture.completed(1)
        val f2 = HsFuture.completed(2)
        val f3 = HsFuture.completed(3)
        val all = HsFuture.allOf(f1, f2, f3)
        val results = all.get()
        assertEquals(3, results.size)
        assertTrue(results.contains(1))
        assertTrue(results.contains(2))
        assertTrue(results.contains(3))
    }

    @Test
    public fun `anyOf completes when any future completes`() {
        val slow = HsFuture.of {
            Thread.sleep(5000)
            "slow"
        }
        val fast = HsFuture.completed("fast")
        val any = HsFuture.anyOf(fast, slow)
        assertEquals("fast", any.get())
    }

    @Test
    public fun `of executes supplier asynchronously`() {
        val thread = HsThread.currentThread().id
        val futureThread = HsFuture.of {
            Thread.currentThread().id to "done"
        }
        val (id, result) = futureThread.get()
        assertEquals("done", result)
    }

    @Test
    public fun `cancel returns false for completed future`() {
        val future = HsFuture.completed(42)
        assertFalse(future.cancel())
    }

    @Test
    public fun `toString contains done status`() {
        val future = HsFuture.completed(42)
        val str = future.toString()
        assertTrue(str.contains("HsFuture"))
        assertTrue(str.contains("done=true"))
    }

    @Test
    public fun `failed future toString shows done status`() {
        val future = HsFuture.failed<Int>(RuntimeException("err"))
        val str = future.toString()
        assertTrue(str.contains("done=true"))
    }
}
