package hasab.lsp.logging

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

public class PerformanceMetrics {

    private val counters = ConcurrentHashMap<String, AtomicLong>()
    private val timings = ConcurrentHashMap<String, MutableList<Long>>()

    public fun incrementCounter(name: String) {
        counters.computeIfAbsent(name) { AtomicLong(0) }.incrementAndGet()
    }

    public fun recordTiming(name: String, durationNs: Long) {
        timings.computeIfAbsent(name) { mutableListOf() }.add(durationNs)
    }

    public inline fun <T> measure(name: String, block: () -> T): T {
        val start = System.nanoTime()
        try {
            return block()
        } finally {
            recordTiming(name, System.nanoTime() - start)
            incrementCounter(name)
        }
    }

    public fun getCounter(name: String): Long = counters[name]?.get() ?: 0L

    public fun getAverageTimingNs(name: String): Double {
        val times = timings[name] ?: return 0.0
        if (times.isEmpty()) return 0.0
        return times.average()
    }

    public fun getCallCount(name: String): Long = timings[name]?.size?.toLong() ?: 0L

    public fun getMinTimingNs(name: String): Long {
        val times = timings[name] ?: return 0L
        return if (times.isEmpty()) 0L else times.min()
    }

    public fun getMaxTimingNs(name: String): Long {
        val times = timings[name] ?: return 0L
        return if (times.isEmpty()) 0L else times.max()
    }

    public fun getP95TimingNs(name: String): Long {
        val times = timings[name] ?: return 0L
        if (times.isEmpty()) return 0L
        val sorted = times.sorted()
        val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
        return sorted[index]
    }

    public fun snapshot(): Map<String, MetricSnapshot> {
        val result = mutableMapOf<String, MetricSnapshot>()
        val allKeys = counters.keys + timings.keys
        for (key in allKeys) {
            result[key] = MetricSnapshot(
                callCount = getCallCount(key),
                counterValue = getCounter(key),
                averageNs = getAverageTimingNs(key),
                minNs = getMinTimingNs(key),
                maxNs = getMaxTimingNs(key),
                p95Ns = getP95TimingNs(key),
            )
        }
        return result
    }

    public fun reset() {
        counters.clear()
        timings.clear()
    }

    public data class MetricSnapshot(
        val callCount: Long,
        val counterValue: Long,
        val averageNs: Double,
        val minNs: Long,
        val maxNs: Long,
        val p95Ns: Long,
    )
}
