package hasab.runtime.concurrency

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsLockTest {

    @Test
    public fun `lock and unlock basic cycle`() {
        val lock = HsLock()
        lock.lock()
        assertTrue(lock.isHeldByCurrentThread())
        lock.unlock()
    }

    @Test
    public fun `tryLock succeeds when not locked`() {
        val lock = HsLock()
        assertTrue(lock.tryLock())
        lock.unlock()
    }

    @Test
    public fun `tryLock fails when already locked by another thread`() {
        val lock = HsLock()
        lock.lock()
        val otherThread = HsThread.of("lock-contention") {
            assertFalse(lock.tryLock())
        }
        otherThread.start()
        otherThread.join(5000)
        lock.unlock()
    }

    @Test
    public fun `isLocked reflects lock state`() {
        val lock = HsLock()
        assertFalse(lock.isLocked())
        lock.lock()
        assertTrue(lock.isLocked())
        lock.unlock()
        assertFalse(lock.isLocked())
    }

    @Test
    public fun `isHeldByCurrentThread is true when current thread holds lock`() {
        val lock = HsLock()
        assertFalse(lock.isHeldByCurrentThread())
        lock.lock()
        assertTrue(lock.isHeldByCurrentThread())
        lock.unlock()
    }

    @Test
    public fun `lock provides mutual exclusion`() {
        val lock = HsLock()
        val counter = AtomicInteger(0)
        val latch = CountDownLatch(1)
        val threads = (1..4).map {
            HsThread.of("mutual-exclusion-$it") {
                latch.await(5, TimeUnit.SECONDS)
                lock.lock()
                try {
                    val current = counter.get()
                    Thread.sleep(5)
                    counter.set(current + 1)
                } finally {
                    lock.unlock()
                }
            }
        }
        threads.forEach { it.start() }
        latch.countDown()
        threads.forEach { it.join(10000) }
        assertEquals(4, counter.get())
    }

    @Test
    public fun `condition await and signal works`() {
        val lock = HsLock()
        val condition = lock.newCondition()
        val signaled = AtomicBoolean(false)

        val waiter = HsThread.of("condition-waiter") {
            lock.lock()
            try {
                condition.await()
                signaled.set(true)
            } finally {
                lock.unlock()
            }
        }

        val signaler = HsThread.of("condition-signaler") {
            Thread.sleep(50)
            lock.lock()
            try {
                condition.signal()
            } finally {
                lock.unlock()
            }
        }

        waiter.start()
        signaler.start()
        waiter.join(5000)
        signaler.join(5000)
        assertTrue(signaled.get())
    }

    @Test
    public fun `condition signalAll wakes all waiters`() {
        val lock = HsLock()
        val condition = lock.newCondition()
        val count = AtomicInteger(0)
        val latch = CountDownLatch(3)

        repeat(3) {
            HsThread.of("waiter-$it") {
                lock.lock()
                try {
                    latch.countDown()
                    condition.await()
                    count.incrementAndGet()
                } finally {
                    lock.unlock()
                }
            }.start()
        }

        latch.await(5, TimeUnit.SECONDS)
        Thread.sleep(50)

        val signaler = HsThread.of("signaler") {
            lock.lock()
            try {
                condition.signalAll()
            } finally {
                lock.unlock()
            }
        }
        signaler.start()
        signaler.join(5000)
        Thread.sleep(200)
        assertEquals(3, count.get())
    }

    @Test
    public fun `condition await with timeout returns false on timeout`() {
        val lock = HsLock()
        val condition = lock.newCondition()
        lock.lock()
        val result = condition.await(50)
        assertFalse(result)
        lock.unlock()
    }

    @Test
    public fun `condition await with timeout returns true when signaled`() {
        val lock = HsLock()
        val condition = lock.newCondition()
        val resultHolder = AtomicBoolean(false)

        val waiter = HsThread.of("timed-waiter") {
            lock.lock()
            try {
                resultHolder.set(condition.await(5000))
            } finally {
                lock.unlock()
            }
        }

        val signaler = HsThread.of("timed-signaler") {
            Thread.sleep(50)
            lock.lock()
            try {
                condition.signal()
            } finally {
                lock.unlock()
            }
        }

        waiter.start()
        signaler.start()
        waiter.join(5000)
        signaler.join(5000)
        assertTrue(resultHolder.get())
    }

    @Test
    public fun `lock reentrant`() {
        val lock = HsLock()
        lock.lock()
        lock.lock()
        assertTrue(lock.isHeldByCurrentThread())
        lock.unlock()
        assertTrue(lock.isHeldByCurrentThread())
        lock.unlock()
        assertFalse(lock.isHeldByCurrentThread())
    }

    @Test
    public fun `tryLock with timeout succeeds`() {
        val lock = HsLock()
        assertTrue(lock.tryLock(100))
        lock.unlock()
    }

    @Test
    public fun `toString contains lock state`() {
        val lock = HsLock()
        val str = lock.toString()
        assertTrue(str.contains("HsLock"))
    }

    @Test
    public fun `condition awaitNanos returns remaining time`() {
        val lock = HsLock()
        val condition = lock.newCondition()
        lock.lock()
        val remaining = condition.awaitNanos(1_000_000)
        assertTrue(remaining <= 0)
        lock.unlock()
    }
}
