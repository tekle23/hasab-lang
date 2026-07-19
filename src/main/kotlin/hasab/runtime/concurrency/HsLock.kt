package hasab.runtime.concurrency

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * Wraps a JVM [ReentrantLock] for the HASAB runtime.
 *
 * Provides mutual exclusion with reentrant acquire semantics.
 * Use [newCondition] to create [HsCondition] instances for
 * thread notification.
 */
public class HsLock {

    private val lock: ReentrantLock = ReentrantLock()

    /**
     * Acquires the lock, blocking until it becomes available.
     */
    public fun lock(): Unit {
        lock.lock()
    }

    /**
     * Releases the lock held by the current thread.
     */
    public fun unlock(): Unit {
        lock.unlock()
    }

    /**
     * Acquires the lock only if it is free at the time of invocation.
     *
     * @return `true` if the lock was acquired, `false` otherwise.
     */
    public fun tryLock(): Boolean = lock.tryLock()

    /**
     * Attempts to acquire the lock within the given [timeout] milliseconds.
     *
     * @return `true` if the lock was acquired, `false` otherwise.
     */
    public fun tryLock(timeout: Long): Boolean {
        return lock.tryLock(timeout, TimeUnit.MILLISECONDS)
    }

    /**
     * Returns whether this lock is currently held by any thread.
     */
    public fun isLocked(): Boolean = lock.isLocked()

    /**
     * Returns whether this lock is held by the current thread.
     */
    public fun isHeldByCurrentThread(): Boolean = lock.isHeldByCurrentThread

    /**
     * Creates a new [HsCondition] bound to this lock.
     */
    public fun newCondition(): HsCondition = HsCondition(lock.newCondition())

    override fun toString(): String = "HsLock(locked=${isLocked()})"
}

/**
 * Wraps a JVM [Condition] for the HASAB runtime.
 *
 * A condition provides thread‑waiting and ‑notification
 * semantics bound to an [HsLock].
 */
public class HsCondition internal constructor(
    private val condition: Condition,
) {

    /**
     * Causes the current thread to wait until signalled or interrupted.
     */
    public fun await(): Unit {
        condition.await()
    }

    /**
     * Causes the current thread to wait until signalled, interrupted,
     * or the specified [millis] timeout elapses.
     *
     * @return `false` if the timeout elapsed, `true` if signalled.
     */
    public fun await(millis: Long): Boolean {
        return condition.await(millis, TimeUnit.MILLISECONDS)
    }

    /**
     * Causes the current thread to wait until signalled, interrupted,
     * or the specified [nanos] timeout elapses.
     *
     * @return the remaining nanos count; zero or negative means timeout.
     */
    public fun awaitNanos(nanos: Long): Long {
        return condition.awaitNanos(nanos)
    }

    /**
     * Wakes up one waiting thread.
     */
    public fun signal(): Unit {
        condition.signal()
    }

    /**
     * Wakes up all waiting threads.
     */
    public fun signalAll(): Unit {
        condition.signalAll()
    }
}
