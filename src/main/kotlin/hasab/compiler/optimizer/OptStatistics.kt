package hasab.compiler.optimizer

/** Accumulates per-pass and aggregate statistics for the optimizer. */
public class OptStatistics {

    private val passStats: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    /** Record statistics produced by a pass. */
    public fun record(passName: String, stats: Map<String, Int>) {
        if (stats.isEmpty()) return
        val target = passStats.getOrPut(passName) { mutableMapOf() }
        for ((metric, value) in stats) {
            target[metric] = (target[metric] ?: 0) + value
        }
    }

    /** Sum a single metric across all passes. */
    public fun totalByMetric(metric: String): Int {
        return passStats.values.sumOf { it[metric] ?: 0 }
    }

    /** Return per-pass statistics for the given pass name. */
    public fun getPassStats(passName: String): Map<String, Int> {
        return passStats[passName]?.toMap() ?: emptyMap()
    }

    /** Human-readable multi-line report of all collected statistics. */
    public fun summary(): String {
        if (passStats.isEmpty()) return "No optimization statistics recorded."
        val sb = StringBuilder()
        sb.appendLine("=== Optimization Statistics ===")
        for ((pass, metrics) in passStats.entries.sortedBy { it.key }) {
            sb.appendLine("  $pass:")
            for ((metric, value) in metrics.entries.sortedBy { it.key }) {
                sb.appendLine("    $metric: $value")
            }
        }
        val total = passStats.values.sumOf { metrics -> metrics.values.sum() }
        sb.appendLine("  Total: $total optimizations")
        return sb.toString()
    }

    /** Reset all recorded statistics. */
    public fun clear() {
        passStats.clear()
    }
}
