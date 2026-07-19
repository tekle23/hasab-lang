package hasab.lsp.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PerformanceMetricsTest {

    @Test
    public fun `incrementCounter`() {
        val metrics = PerformanceMetrics()
        metrics.incrementCounter("test.counter")
        metrics.incrementCounter("test.counter")
        metrics.incrementCounter("test.counter")
        assertEquals(3L, metrics.getCounter("test.counter"))
    }

    @Test
    public fun `recordTiming`() {
        val metrics = PerformanceMetrics()
        metrics.recordTiming("test.timing", 1000L)
        metrics.recordTiming("test.timing", 2000L)
        assertEquals(2L, metrics.getCallCount("test.timing"))
    }

    @Test
    public fun `measure times block execution`() {
        val metrics = PerformanceMetrics()
        val result = metrics.measure("test.measure") {
            42
        }
        assertEquals(42, result)
        assertEquals(1L, metrics.getCallCount("test.measure"))
        assertTrue(metrics.getAverageTimingNs("test.measure") > 0L)
    }

    @Test
    public fun `getCounter returns count`() {
        val metrics = PerformanceMetrics()
        assertEquals(0L, metrics.getCounter("nonexistent"))
        metrics.incrementCounter("counter1")
        metrics.incrementCounter("counter1")
        assertEquals(2L, metrics.getCounter("counter1"))
    }

    @Test
    public fun `getAverageTimingNs returns average`() {
        val metrics = PerformanceMetrics()
        metrics.recordTiming("avg.test", 100L)
        metrics.recordTiming("avg.test", 200L)
        metrics.recordTiming("avg.test", 300L)
        val avg = metrics.getAverageTimingNs("avg.test")
        assertEquals(200.0, avg, 0.01)
        assertEquals(0.0, metrics.getAverageTimingNs("nonexistent"), 0.01)
    }

    @Test
    public fun `reset clears all data`() {
        val metrics = PerformanceMetrics()
        metrics.incrementCounter("counter1")
        metrics.recordTiming("timing1", 100L)
        metrics.reset()
        assertEquals(0L, metrics.getCounter("counter1"))
        assertEquals(0L, metrics.getCallCount("timing1"))
        assertEquals(0.0, metrics.getAverageTimingNs("timing1"), 0.01)
    }

    @Test
    public fun `snapshot returns all metrics`() {
        val metrics = PerformanceMetrics()
        metrics.incrementCounter("counter1")
        metrics.recordTiming("timing1", 100L)
        metrics.recordTiming("timing1", 200L)
        val snapshot = metrics.snapshot()
        assertNotNull(snapshot)
        assertTrue(snapshot.containsKey("counter1"))
        assertTrue(snapshot.containsKey("timing1"))
        assertEquals(1L, snapshot["counter1"]!!.counterValue)
        assertEquals(2L, snapshot["timing1"]!!.callCount)
    }

    @Test
    public fun `getCallCount returns number of timing records`() {
        val metrics = PerformanceMetrics()
        assertEquals(0L, metrics.getCallCount("nonexistent"))
        metrics.recordTiming("calls.test", 100L)
        metrics.recordTiming("calls.test", 200L)
        metrics.recordTiming("calls.test", 300L)
        assertEquals(3L, metrics.getCallCount("calls.test"))
    }

    @Test
    public fun `getP95TimingNs`() {
        val metrics = PerformanceMetrics()
        for (i in 1L..100L) {
            metrics.recordTiming("p95.test", i)
        }
        val p95 = metrics.getP95TimingNs("p95.test")
        assertTrue(p95 >= 95L, "P95 should be at least 95, got $p95")
        assertTrue(p95 <= 100L, "P95 should be at most 100, got $p95")
        assertEquals(0L, metrics.getP95TimingNs("nonexistent"))
    }
}
