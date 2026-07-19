package hasab.compiler.optimizer

import hasab.compiler.hir.cfg.HirCfgModule

/** Result of a single optimization pass run. */
public data class PassResult(
    public val module: HirCfgModule,
    public val changed: Boolean,
    public val stats: Map<String, Int> = emptyMap(),
)

/** Context passed to each optimization pass. */
public data class PassContext(
    public val statistics: OptStatistics,
)

/** Interface for an optimization pass. */
public interface OptimizationPass {
    public val name: String
    public val description: String
    public fun run(module: HirCfgModule, context: PassContext): PassResult
}
