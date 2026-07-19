package hasab.compiler.optimizer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OptStatisticsTest {

    @Test
    fun `starts empty`() {
        val stats = OptStatistics()
        assertEquals(0, stats.totalByMetric("constants_folded"))
    }

    @Test
    fun `record and totalByMetric`() {
        val stats = OptStatistics()
        stats.record("ConstantFolder", mapOf("constants_folded" to 5))
        assertEquals(5, stats.totalByMetric("constants_folded"))
    }

    @Test
    fun `accumulate across multiple passes`() {
        val stats = OptStatistics()
        stats.record("ConstantFolder", mapOf("constants_folded" to 3))
        stats.record("ConstantPropagation", mapOf("constants_folded" to 2))
        assertEquals(5, stats.totalByMetric("constants_folded"))
    }

    @Test
    fun `getPassStats returns empty for unknown pass`() {
        val stats = OptStatistics()
        assertTrue(stats.getPassStats("Unknown").isEmpty())
    }

    @Test
    fun `getPassStats returns per-pass stats`() {
        val stats = OptStatistics()
        stats.record("ConstantFolder", mapOf("constants_folded" to 7))
        val passStats = stats.getPassStats("ConstantFolder")
        assertEquals(7, passStats["constants_folded"])
    }

    @Test
    fun `summary contains pass info`() {
        val stats = OptStatistics()
        stats.record("ConstantFolder", mapOf("constants_folded" to 3))
        val summary = stats.summary()
        assertTrue(summary.contains("ConstantFolder"))
        assertTrue(summary.contains("constants_folded"))
    }

    @Test
    fun `summary empty when no stats`() {
        val stats = OptStatistics()
        val summary = stats.summary()
        assertTrue(summary.contains("No optimization"))
    }

    @Test
    fun `clear resets statistics`() {
        val stats = OptStatistics()
        stats.record("ConstantFolder", mapOf("constants_folded" to 10))
        stats.clear()
        assertEquals(0, stats.totalByMetric("constants_folded"))
    }
}
