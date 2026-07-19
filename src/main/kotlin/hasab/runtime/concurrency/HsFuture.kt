package hasab.runtime.concurrency

import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Wraps a JVM [CompletableFuture] for the HASAB runtime.
 *
 * Provides asynchronous result retrieval and composition.
 * Use [HsFuture.of] to create an async future from a [supplier],
 * or [completed] / [failed] for immediate values.
 */
public class HsFuture<T> private constructor(
    private val future: CompletableFuture<T>,
) {

    /**
     * Returns `true` if this future has completed (normally or exceptionally).
     */
    public val isDone: Boolean get() = future.isDone

    /**
     * Returns `true` if this future was cancelled before completion.
     */
    public val isCancelled: Boolean get() = future.isCancelled

    /**
     * Waits indefinitely for the result.
     *
     * @throws CancellationException if the future was cancelled.
     * @throws ExecutionException if the future completed exceptionally.
     */
    public fun get(): T = future.get()

    /**
     * Waits for the result for at most [timeout] milliseconds.
     *
     * @throws CancellationException if the future was cancelled.
     * @throws ExecutionException if the future completed exceptionally.
     */
    public fun get(timeout: Long): T = future.get(timeout, TimeUnit.MILLISECONDS)

    /**
     * Attempts to cancel execution of this future.
     *
     * @return `false` if the future could not be cancelled (already completed).
     */
    public fun cancel(): Boolean = future.cancel(true)

    /**
     * Returns a new [HsFuture] that applies [fn] to this future's result when completed.
     */
    public fun thenApply(fn: (T) -> T): HsFuture<T> = HsFuture(future.thenApply(fn))

    /**
     * Returns a new [HsFuture] that accepts [action] when this future completes.
     */
    public fun thenAccept(action: (T) -> Unit): HsFuture<T> {
        future.thenAccept(action)
        return this
    }

    /**
     * Returns a new [HsFuture] that runs [action] when this future completes.
     */
    public fun thenRun(action: () -> Unit): HsFuture<T> {
        future.thenRun(action)
        return this
    }

    /**
     * Returns a new [HsFuture] that handles exceptional completions via [fn].
     */
    public fun exceptionally(fn: (Throwable) -> T): HsFuture<T> =
        HsFuture(future.exceptionally(fn))

    /**
     * Returns a new [HsFuture] that invokes [action] when this future completes.
     */
    @Suppress("UNCHECKED_CAST")
    public fun whenComplete(action: (T?, Throwable?) -> Unit): HsFuture<T> =
        HsFuture(future.whenComplete { result, error -> action(result, error) })

    override fun toString(): String = "HsFuture(done=$isDone, cancelled=$isCancelled)"

    public companion object {

        /**
         * Asynchronously evaluates [supplier] and returns an [HsFuture] of the result.
         */
        public fun <T> of(supplier: () -> T): HsFuture<T> =
            HsFuture(CompletableFuture.supplyAsync { supplier() })

        /**
         * Returns an [HsFuture] that is already completed with [value].
         */
        public fun <T> completed(value: T): HsFuture<T> =
            HsFuture(CompletableFuture.completedFuture(value))

        /**
         * Returns an [HsFuture] that is already completed exceptionally with [error].
         */
        @Suppress("UNCHECKED_CAST")
        public fun <T> failed(error: Throwable): HsFuture<T> {
            val f = CompletableFuture<T>()
            f.completeExceptionally(error)
            return HsFuture(f)
        }

        /**
         * Returns an [HsFuture] that completes when all of the given [futures] complete.
         *
         * The result is a list of the individual future results.
         */
        @Suppress("UNCHECKED_CAST")
        public fun allOf(vararg futures: HsFuture<*>): HsFuture<List<Any?>> {
            val raw = futures.map { it.future }
            val combined = CompletableFuture.allOf(*raw.toTypedArray())
                .thenApply { raw.map { f -> f.get() } }
            return HsFuture(combined)
        }

        /**
         * Returns an [HsFuture] that completes when any of the given [futures] completes.
         */
        @Suppress("UNCHECKED_CAST")
        public fun anyOf(vararg futures: HsFuture<*>): HsFuture<Any?> {
            val raw = futures.map { it.future }
            val combined = CompletableFuture.anyOf(*raw.toTypedArray())
            return HsFuture(combined)
        }
    }
}
