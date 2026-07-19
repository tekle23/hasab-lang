package hasab.compiler.optimizer

import hasab.compiler.hir.cfg.HirCfgModule
import hasab.compiler.optimizer.passes.BranchSimplifier
import hasab.compiler.optimizer.passes.CFGOptimizer
import hasab.compiler.optimizer.passes.ConstantFolder
import hasab.compiler.optimizer.passes.ConstantPropagation
import hasab.compiler.optimizer.passes.CopyPropagation
import hasab.compiler.optimizer.passes.DeadCodeElimination

/** Result of a full optimization run. */
public data class OptimizationResult(
    public val module: HirCfgModule,
    public val statistics: OptStatistics,
)

/** Top-level entry point for the HASAB optimizer pipeline. */
public class OptimizationManager(public val profile: OptProfile = OptProfile.Release) {

    /** Optimizer passes applied in Release mode, in pipeline order. */
    private val releasePasses: List<OptimizationPass> = listOf(
        ConstantFolder(),
        ConstantPropagation(),
        CopyPropagation(),
        BranchSimplifier(),
        DeadCodeElimination(),
        CFGOptimizer(),
    )

    /**
     * Run the optimizer on [module] according to the selected [profile].
     *
     * In [OptProfile.Debug] mode no passes are executed and the module
     * is returned as-is. In [OptProfile.Release] mode all registered
     * passes run to a fixed point via [PassManager].
     */
    public fun optimize(module: HirCfgModule): OptimizationResult {
        if (profile == OptProfile.Debug) {
            return OptimizationResult(module, OptStatistics())
        }

        val statistics = OptStatistics()
        val context = PassContext(statistics)
        val manager = PassManager(releasePasses)
        val optimized = manager.run(module, context)
        return OptimizationResult(optimized, statistics)
    }
}
