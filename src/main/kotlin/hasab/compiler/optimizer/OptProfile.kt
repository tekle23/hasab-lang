package hasab.compiler.optimizer

/** Optimization profile presets. */
public enum class OptProfile {
    /** Minimal or no optimizations — used for debug builds. */
    Debug,
    /** All optimizations enabled — used for release builds. */
    Release,
}
