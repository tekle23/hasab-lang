package hasab.runtime.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsProfilerTest {

    @Test
    public fun `timer creates and starts timer`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("test-timer")
        assertTrue(timer.isRunning())
    }

    @Test
    public fun `timer stop returns positive elapsed nanos`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("stop-test")
        Thread.sleep(10)
        val elapsed = timer.stop()
        assertTrue(elapsed > 0)
    }

    @Test
    public fun `timer stop sets isRunning to false`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("stop-running")
        timer.stop()
        assertFalse(timer.isRunning())
    }

    @Test
    public fun `timer elapsedMillis is positive after stop`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("elapsed-test")
        Thread.sleep(10)
        timer.stop()
        assertTrue(timer.elapsedMillis() > 0.0)
    }

    @Test
    public fun `timer reset clears accumulated time`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("reset-test")
        Thread.sleep(10)
        timer.stop()
        assertTrue(timer.elapsedMillis() > 0.0)
        timer.reset()
        assertEquals(0.0, timer.elapsedMillis())
    }

    @Test
    public fun `timer reset sets isRunning to false`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("reset-running")
        timer.reset()
        assertFalse(timer.isRunning())
    }

    @Test
    public fun `timer toString contains name and time`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("tostring-test")
        timer.stop()
        val str = timer.toString()
        assertTrue(str.contains("tostring-test"))
        assertTrue(str.contains("ms"))
    }

    @Test
    public fun `measure returns elapsed nanoseconds`() {
        HsProfiler.resetAll()
        val elapsed = HsProfiler.measure("measure-test") {
            Thread.sleep(10)
        }
        assertTrue(elapsed > 0)
    }

    @Test
    public fun `measure executes block`() {
        HsProfiler.resetAll()
        var executed = false
        HsProfiler.measure("exec-test") {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    public fun `report contains header and footer`() {
        HsProfiler.resetAll()
        val report = HsProfiler.report()
        assertTrue(report.contains("=== HASAB Profiler Report ==="))
        assertTrue(report.contains("=== End Report ==="))
    }

    @Test
    public fun `report shows no timers message when empty`() {
        HsProfiler.resetAll()
        val report = HsProfiler.report()
        assertTrue(report.contains("no timers recorded"))
    }

    @Test
    public fun `report shows timer entries after recording`() {
        HsProfiler.resetAll()
        HsProfiler.measure("report-test") { Thread.sleep(5) }
        val report = HsProfiler.report()
        assertTrue(report.contains("report-test"))
        assertTrue(report.contains("ms"))
    }

    @Test
    public fun `resetAll clears all timers`() {
        HsProfiler.resetAll()
        HsProfiler.measure("reset-a") {}
        HsProfiler.measure("reset-b") {}
        assertTrue(HsProfiler.getTimers().isNotEmpty())
        HsProfiler.resetAll()
        assertTrue(HsProfiler.getTimers().isEmpty())
    }

    @Test
    public fun `getTimers returns snapshot`() {
        HsProfiler.resetAll()
        HsProfiler.measure("snap-test") {}
        val timers = HsProfiler.getTimers()
        assertTrue(timers.isNotEmpty())
        assertTrue(timers.containsKey("snap-test"))
    }

    @Test
    public fun `getTimers returns independent copy`() {
        HsProfiler.resetAll()
        HsProfiler.measure("copy-test") {}
        val timers1 = HsProfiler.getTimers()
        HsProfiler.measure("copy-test-2") {}
        val timers2 = HsProfiler.getTimers()
        assertEquals(timers1.size + 1, timers2.size)
    }

    @Test
    public fun `multiple timers are tracked independently`() {
        HsProfiler.resetAll()
        val t1 = HsProfiler.timer("multi-a")
        Thread.sleep(10)
        t1.stop()
        val t2 = HsProfiler.timer("multi-b")
        Thread.sleep(10)
        t2.stop()
        val timers = HsProfiler.getTimers()
        assertEquals(2, timers.size)
        assertTrue(timers.containsKey("multi-a"))
        assertTrue(timers.containsKey("multi-b"))
    }

    @Test
    public fun `timer name is preserved`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("my-precise-name")
        timer.stop()
        val timers = HsProfiler.getTimers()
        assertTrue(timers.containsKey("my-precise-name"))
    }

    @Test
    public fun `timer start restarts timing`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("restart-test")
        Thread.sleep(10)
        timer.stop()
        val firstElapsed = timer.elapsedMillis()
        timer.start()
        assertTrue(timer.isRunning())
        Thread.sleep(10)
        timer.stop()
        assertTrue(timer.elapsedMillis() > 0)
    }

    @Test
    public fun `timer stop is idempotent`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("idempotent")
        Thread.sleep(10)
        val first = timer.stop()
        val second = timer.stop()
        assertEquals(first, second)
    }
}
