package hasab.runtime.concurrency

/**
 * Wraps a JVM [Thread] for the HASAB runtime.
 *
 * Provides a safe, idiomatic wrapper around [java.lang.Thread] including
 * factory methods, priority control, and exception handlers.
 */
public class HsThread private constructor(
    internal val thread: Thread,
    private val runnable: () -> Unit,
) {

    public val id: Long get() = thread.id

    public val name: String get() = thread.name

    public val isAlive: Boolean get() = thread.isAlive

    public val isDaemon: Boolean get() = thread.isDaemon

    public var daemon: Boolean
        get() = thread.isDaemon
        set(value) { thread.setDaemon(value) }

    /**
     * Starts this thread, invoking the wrapped [runnable].
     */
    public fun start(): Unit {
        thread.start()
    }

    /**
     * Waits indefinitely for this thread to die.
     */
    public fun join(): Unit {
        thread.join()
    }

    /**
     * Waits at most [millis] milliseconds for this thread to die.
     */
    public fun join(millis: Long): Unit {
        thread.join(millis)
    }

    /**
     * Interrupts this thread.
     */
    public fun interrupt(): Unit {
        thread.interrupt()
    }

    /**
     * Returns whether this thread has been interrupted.
     */
    public fun isInterrupted(): Boolean = thread.isInterrupted

    /**
     * Sets the thread priority (1-10).
     */
    public fun setPriority(priority: Int): Unit {
        thread.priority = priority
    }

    /**
     * Returns the current thread priority.
     */
    public fun getPriority(): Int = thread.priority

    /**
     * Sets an uncaught exception handler for this thread.
     */
    public fun setExceptionHandler(handler: (HsThread, Throwable) -> Unit): Unit {
        thread.setUncaughtExceptionHandler { _, e ->
            handler(this, e)
        }
    }

    override fun toString(): String = "HsThread(id=$id, name='$name', alive=$isAlive)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsThread) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    public companion object {

        /**
         * Returns the [HsThread] wrapping the currently executing thread.
         */
        public fun currentThread(): HsThread {
            val jt = Thread.currentThread()
            val existing = activeThreads.find { it.thread == jt }
            if (existing != null) return existing
            val wrapper = HsThread(jt, {})
            activeThreads.add(wrapper)
            return wrapper
        }

        /**
         * Causes the currently executing thread to sleep for [millis] milliseconds.
         */
        public fun sleep(millis: Long): Unit {
            Thread.sleep(millis)
        }

        /**
         * A hint to the scheduler that the current thread is willing to yield its CPU time.
         */
        public fun yield(): Unit {
            Thread.yield()
        }

        /**
         * Returns the number of active threads in the current thread group.
         */
        public fun activeCount(): Int = Thread.activeCount()

        /**
         * Returns a list of all live [HsThread] wrappers.
         */
        public fun getAllThreads(): List<HsThread> = activeThreads.toList()

        /**
         * Creates a new [HsThread] with the given [name], [daemon] flag, and [runnable].
         */
        public fun of(name: String, daemon: Boolean = false, runnable: () -> Unit): HsThread {
            val jt = Thread { runnable() }
            jt.name = name
            jt.isDaemon = daemon
            val wrapper = HsThread(jt, runnable)
            activeThreads.add(wrapper)
            return wrapper
        }

        private val activeThreads = mutableListOf<HsThread>()
    }
}
