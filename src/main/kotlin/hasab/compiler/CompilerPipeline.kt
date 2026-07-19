package hasab.compiler

import hasab.compiler.backend.*
import hasab.compiler.backend.javac.JavacError
import hasab.compiler.backend.javac.JavacInvoker
import hasab.compiler.backend.javac.JavacResult
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.ParseResult
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.hir.HIRGenerator
import hasab.compiler.optimizer.OptimizationManager
import hasab.compiler.optimizer.OptProfile
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.types.TypeCheckResult
import hasab.compiler.types.TypeChecker
import java.io.File

public data class PipelineConfig(
    val backendType: BackendType = BackendType.JAVA_SOURCE,
    val optProfile: OptProfile = OptProfile.Debug,
)

public data class SourceInput(
    val fileName: String,
    val sourceCode: String,
)

public data class PipelineResult(
    val success: Boolean,
    val generatedSources: Map<String, String>,
    val mainClassName: String,
    val sourceMap: SourceMap,
    val compileErrors: List<SourceMappedError>,
    val javaErrors: List<SourceMappedError>,
    val modules: List<CompiledModule>,
)

public data class SourceMappedError(
    val sourceFile: String,
    val sourceLine: Int,
    val sourceColumn: Int,
    val message: String,
    val severity: String,
    val generatedFile: String = "",
    val generatedLine: Int = 0,
)

public class CompilerPipeline(
    private val config: PipelineConfig = PipelineConfig(),
    private val backend: CompilerBackend = createDefaultBackend(config.backendType),
) {

    private val javacInvoker = JavacInvoker()

    public fun compileProject(
        sourceFiles: List<SourceInput>,
        outputDir: File,
    ): PipelineResult {
        val sourceMap = SourceMap()
        val modules = mutableListOf<CompiledModule>()
        val compileErrors = mutableListOf<SourceMappedError>()

        for (input in sourceFiles) {
            val result = compileSingleFile(input, sourceMap)
            if (result.first != null) {
                modules.add(result.first!!)
            }
            compileErrors.addAll(result.second)
        }

        if (compileErrors.any { it.severity == "error" }) {
            return PipelineResult(
                success = false,
                generatedSources = emptyMap(),
                mainClassName = "",
                sourceMap = sourceMap,
                compileErrors = compileErrors,
                javaErrors = emptyList(),
                modules = modules,
            )
        }

        val backendContext = BackendContext(modules = modules, sourceMap = sourceMap)
        val backendOutput = backend.generate(backendContext)

        val classesDir = File(outputDir, "classes")
        val javaFiles = writeGeneratedSources(backendOutput, classesDir)

        val javacResult = javacInvoker.compileDirectory(classesDir, classesDir)

        val javaErrors = mutableListOf<SourceMappedError>()
        if (!javacResult.success) {
            for (err in javacResult.errors) {
                val sourceLoc = sourceMap.translateCompileError(err.file, err.line)
                javaErrors.add(
                    SourceMappedError(
                        sourceFile = sourceLoc?.file ?: err.file,
                        sourceLine = sourceLoc?.line ?: err.line,
                        sourceColumn = sourceLoc?.column ?: err.column,
                        message = err.message,
                        severity = err.severity,
                        generatedFile = err.file,
                        generatedLine = err.line,
                    )
                )
            }
        }

        return PipelineResult(
            success = javacResult.success && compileErrors.none { it.severity == "error" },
            generatedSources = backendOutput.generatedSources,
            mainClassName = backendOutput.mainClassName,
            sourceMap = sourceMap,
            compileErrors = compileErrors,
            javaErrors = javaErrors,
            modules = modules,
        )
    }

    public fun compileProjectFromDirectory(
        projectDir: File,
        sourceDir: File,
        outputDir: File,
    ): PipelineResult {
        val hasFiles = sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "has" }
            .toList()

        if (hasFiles.isEmpty()) {
            return PipelineResult(
                success = false,
                generatedSources = emptyMap(),
                mainClassName = "",
                sourceMap = SourceMap(),
                compileErrors = listOf(SourceMappedError("", 0, 0, "No .has files found in ${sourceDir.path}", "error")),
                javaErrors = emptyList(),
                modules = emptyList(),
            )
        }

        val inputs = hasFiles.map { file ->
            val relativePath = file.relativeTo(projectDir).path
            SourceInput(fileName = relativePath, sourceCode = file.readText(Charsets.UTF_8))
        }

        return compileProject(inputs, outputDir)
    }

    private fun compileSingleFile(
        input: SourceInput,
        sourceMap: SourceMap,
    ): Pair<CompiledModule?, List<SourceMappedError>> {
        val errors = mutableListOf<SourceMappedError>()
        val source = SourceFile(input.fileName, input.sourceCode)

        val lexerResult = Lexer(source).tokenize()
        val parseResult: ParseResult = Parser(lexerResult).parse()

        if (parseResult.hasErrors) {
            for (diag in parseResult.diagnostics) {
                errors.add(
                    SourceMappedError(
                        sourceFile = input.fileName,
                        sourceLine = diag.range.start.line,
                        sourceColumn = diag.range.start.column,
                        message = diag.message,
                        severity = diag.severity.name.lowercase(),
                    )
                )
            }
            return Pair(null, errors)
        }

        val semanticModel: SemanticModel = SemanticAnalyzer().analyze(parseResult.module)
        if (semanticModel.hasErrors) {
            for (diag in semanticModel.diagnostics) {
                errors.add(
                    SourceMappedError(
                        sourceFile = input.fileName,
                        sourceLine = diag.range.start.line,
                        sourceColumn = diag.range.start.column,
                        message = diag.message,
                        severity = diag.severity.name.lowercase(),
                    )
                )
            }
        }

        val typeChecker = TypeChecker()
        val typeCheckResult: TypeCheckResult = typeChecker.check(parseResult.module)
        if (typeCheckResult.hasErrors) {
            for (diag in typeCheckResult.diagnostics) {
                errors.add(
                    SourceMappedError(
                        sourceFile = input.fileName,
                        sourceLine = diag.range.start.line,
                        sourceColumn = diag.range.start.column,
                        message = diag.message,
                        severity = diag.severity.name.lowercase(),
                    )
                )
            }
        }

        if (errors.any { it.severity == "error" }) {
            return Pair(null, errors)
        }

        if (config.optProfile != OptProfile.Debug) {
            val hirGenerator = HIRGenerator()
            hirGenerator.generate(typeCheckResult, parseResult.module)
            val optimizationManager = OptimizationManager(config.optProfile)
            // HIR optimization runs on the generated HIR but does not feed back into
            // the AST-based backend yet. This is wired for future JVM backend.
            // For now, the Java source backend uses the AST directly.
        }

        sourceMap.recordSourceFileLineOffset(input.fileName, 0)

        val module = CompiledModule(
            fileName = input.fileName,
            ast = parseResult.module,
            typeCheckResult = typeCheckResult,
        )

        return Pair(module, errors)
    }

    private fun writeGeneratedSources(output: BackendOutput, classesDir: File): List<File> {
        classesDir.mkdirs()
        val javaFiles = mutableListOf<File>()

        for ((path, source) in output.generatedSources) {
            val file = File(classesDir, path)
            file.parentFile?.mkdirs()
            file.writeText(source, Charsets.UTF_8)
            javaFiles.add(file)
        }

        return javaFiles
    }

    public companion object {
        private fun createDefaultBackend(type: BackendType): CompilerBackend {
            return when (type) {
                BackendType.JAVA_SOURCE -> {
                    val generator = JavaSourceGenerator()
                    JavaSourceBackend(generator)
                }
                BackendType.JVM -> {
                    throw UnsupportedOperationException("JVM backend not yet integrated into pipeline")
                }
                BackendType.WASM -> {
                    throw UnsupportedOperationException("WASM backend not yet available")
                }
                BackendType.NATIVE -> {
                    throw UnsupportedOperationException("Native backend not yet available")
                }
            }
        }
    }
}
