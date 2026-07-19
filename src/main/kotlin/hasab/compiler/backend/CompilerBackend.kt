package hasab.compiler.backend

import hasab.compiler.frontend.ast.Module
import hasab.compiler.types.TypeCheckResult

public enum class BackendType {
    JAVA_SOURCE,
    JVM,
    WASM,
    NATIVE,
}

public data class BackendOutput(
    val generatedSources: Map<String, String>,
    val mainClassName: String,
)

public data class CompiledModule(
    val fileName: String,
    val ast: Module,
    val typeCheckResult: TypeCheckResult,
)

public data class BackendContext(
    val modules: List<CompiledModule>,
    val sourceMap: SourceMap,
)

public interface CompilerBackend {
    public val backendType: BackendType
    public fun generate(context: BackendContext): BackendOutput
}
