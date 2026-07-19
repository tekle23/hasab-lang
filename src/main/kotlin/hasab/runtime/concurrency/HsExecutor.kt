package hasab.runtime.concurrency

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Wraps a JVM [ExecutorService] for the HASAB runtime.
 *
 * Manages a pool of threads for executing tasks asynchronously.
 * Defaults to a fixed thread pool sized to the number of available processors.
 */
public class HsExecutor @JvmOverloads constructor(
    threadCount: Int = Runtime.getRuntime().availableProcessors(),
) {

    private val executor: ExecutorService = Executors.newFixedThreadPool(threadCount)

    private constructor(executor: ExecutorService, unused: Unit) : this(1) {
        val field = HsExecutor::class.java.getDeclaredField("executor")
        field.isAccessible = true
        field.set(this, executor)
    }

    /**
     * Submits [task] for asynchronous execution.
     *
     * @return an [HsFuture] representing the pending result.
     */
    public fun submit(task: () -> Any?): HsFuture<Any?> {
        val future = executor.submit(Callable<Any?> { task() })
        return HsFuture.of { future.get() }
    }

    /**
     * Submits all [tasks] for asynchronous execution.
     *
     * @return a list of [HsFuture] instances corresponding to each task.
     */
    public fun submitAll(tasks: List<() -> Any?>): List<HsFuture<Any?>> =
        tasks.map { submit(it) }

    /**
     * Initiates an orderly shutdown; previously submitted tasks execute but no new tasks accepted.
     */
    public fun shutdown(): Unit {
        executor.shutdown()
    }

    /**
     * Attempts to stop all actively executing tasks and halts waiting tasks.
     */
    public fun shutdownNow(): Unit {
        executor.shutdownNow()
    }

    /**
     * Returns `true` if this executor has been shut down.
     */
    public val isShutdown: Boolean get() = executor.isShutdown

    /**
     * Returns `true` if all tasks have completed after shutdown.
     */
    public val isTerminated: Boolean get() = executor.isTerminated

    /**
     * Blocks until all tasks have completed after shutdown, or [timeout] milliseconds elapse.
     *
     * @return `true` if terminated, `false` if timeout elapsed.
     */
    public fun awaitTermination(timeout: Long): Boolean =
        executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)

    public companion object {

        /**
         * Creates a fixed thread pool with [threads] threads.
         */
        public fun fixedThreadPool(threads: Int): HsExecutor {
            val e = Executors.newFixedThreadPool(threads)
            return HsExecutor(e, Unit)
        }

        /**
         * Creates a single‑thread executor.
         */
        public fun singleThread(): HsExecutor {
            val e = Executors.newSingleThreadExecutor()
            return HsExecutor(e, Unit)
        }

        /**
         * Creates a cached thread pool that creates new threads as needed.
         */
        public fun cachedThreadPool(): HsExecutor {
            val e = Executors.newCachedThreadPool()
            return HsExecutor(e, Unit)
        }

        /**
         * Creates an executor backed by Java 21 virtual threads when available;
         * falls back to a fixed thread pool equal to the number of processors.
         */
        public fun virtualThreads(): HsExecutor {
            return try {
                val newThreadPerTaskExecutor = Executors::class.java
                    .getMethod("newVirtualThreadPerTaskExecutor")
                val executor = newThreadPerTaskExecutor.invoke(null) as ExecutorService
                HsExecutor(executor, Unit)
            } catch (_: Exception) {
                fixedThreadPool(Runtime.getRuntime().availableProcessors())
            }
        }
    }
}
