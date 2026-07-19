package hasab.compiler.optimizer

import hasab.compiler.hir.cfg.HirCfgModule

/**
 * Runs a list of optimization passes iteratively until a fixed point
 * is reached (no pass reports a change) or the iteration limit is hit.
 */
public class PassManager(private val passes: List<OptimizationPass>) {

    /**
     * Run all passes on [module] repeatedly until none of them
     * report a change, or [maxIterations] rounds have completed.
     */
    public fun run(module: HirCfgModule, context: PassContext, maxIterations: Int = 10): HirCfgModule {
        var current = module
        for (iteration in 1..maxIterations) {
            var anyChanged = false
            for (pass in passes) {
                val result = pass.run(current, context)
                context.statistics.record(pass.name, result.stats)
                if (result.changed) {
                    current = result.module
                    anyChanged = true
                }
            }
            if (!anyChanged) break
        }
        return current
    }
}
