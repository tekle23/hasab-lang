package hasab.runtime.services

/**
 * Simple profiling utilities for measuring and reporting execution times.
 *
 * Timers are identified by name and stored in the global [timers] map.
 */
public object HsProfiler {

    private val timers: MutableMap<String, Timer> = mutableMapOf()

    /**
     * A named timer that tracks elapsed time via [System.nanoTime].
     */
    public class Timer(private val name: String) {

        private var startTime: Long = 0L
        private var elapsedNanos: Long = 0L
        private var running: Boolean = false

        /**
         * Starts (or restarts) this timer.
         */
        public fun start(): Unit {
            startTime = System.nanoTime()
            running = true
        }

        /**
         * Stops this timer and returns the elapsed time in nanoseconds
         * since the last [start] call.
         */
        public fun stop(): Long {
            if (running) {
                elapsedNanos = System.nanoTime() - startTime
                running = false
            }
            return elapsedNanos
        }

        /**
         * Returns the elapsed time in milliseconds.
         */
        public fun elapsedMillis(): Double = elapsedNanos / 1_000_000.0

        /**
         * Resets accumulated time and stops the timer.
         */
        public fun reset(): Unit {
            startTime = 0L
            elapsedNanos = 0L
            running = false
        }

        /**
         * Returns `true` when the timer is currently running.
         */
        public fun isRunning(): Boolean = running

        override fun toString(): String = "Timer($name, ${elapsedMillis()}ms)"
    }

    /**
     * Returns a [Timer] registered under [name], creating it if it does not exist.
     *
     * The timer is automatically started.
     */
    public fun timer(name: String): Timer {
        val t = timers.getOrPut(name) { Timer(name) }
        t.start()
        return t
    }

    /**
     * Executes [block], measures its wall-clock time, and returns the elapsed nanoseconds.
     *
     * A timer named [name] is also registered so the result appears in [report].
     */
    public fun measure(name: String, block: () -> Unit): Long {
        val t = timer(name)
        block()
        return t.stop()
    }

    /**
     * Returns a formatted multi-line report of all recorded timers.
     */
    public fun report(): String = buildString {
        appendLine("=== HASAB Profiler Report ===")
        if (timers.isEmpty()) {
            appendLine("  (no timers recorded)")
        } else {
            for ((name, timer) in timers.entries.sortedByDescending { it.value.elapsedMillis() }) {
                appendLine("  ${name.padEnd(30)} ${String.format("%12.3f", timer.elapsedMillis())} ms")
            }
        }
        appendLine("=== End Report ===")
    }

    /**
     * Resets all registered timers.
     */
    public fun resetAll(): Unit {
        for (timer in timers.values) {
            timer.reset()
        }
        timers.clear()
    }

    /**
     * Returns a snapshot of all registered timers.
     */
    public fun getTimers(): Map<String, Timer> = timers.toMap()
}
