package hasab.compiler

import hasab.compiler.backend.BackendType
import hasab.compiler.backend.HasabToJavaCompiler
import hasab.compiler.backend.JavaSourceGenerator
import hasab.compiler.backend.SourceMap
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.types.TypeChecker
import hasab.compiler.optimizer.OptProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class CompilerRegressionTest {

    private fun pipeline(): CompilerPipeline = CompilerPipeline(
        config = PipelineConfig(backendType = BackendType.JAVA_SOURCE, optProfile = OptProfile.Debug)
    )

    private fun examplesDir(): File {
        val projectRoot = System.getProperty("user.dir")
        return File(projectRoot, "examples")
    }

    private fun discoverExamples(): List<Pair<String, String>> {
        val dir = examplesDir()
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "has" || f.extension == "hasab" }
            ?.sortedBy { it.name }
            ?.map { it.nameWithoutExtension to it.readText() }
            ?: emptyList()
    }

    private fun stripExpectedComments(source: String): String {
        return source.lines()
            .filter { !it.trimStart().startsWith("// EXPECTED:") }
            .joinToString("\n")
    }

    @Test
    fun `debug simple program`() {
        val code = """
            fn main() {
                let name = "HASAB";
                println(name);
            }
        """.trimIndent()
        val source = SourceFile("test.has", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val semanticResult = SemanticAnalyzer().analyze(parseResult.module)
        val typeResult = TypeChecker().check(parseResult.module)
        println("Parse errors: ${parseResult.hasErrors}")
        println("Semantic errors: ${semanticResult.hasErrors}")
        println("Semantic: ${semanticResult.errors.joinToString("; ") { it.message }}")
        println("Type errors: ${typeResult.hasErrors}")
        println("Type: ${typeResult.diagnostics.joinToString("; ") { it.message }}")
    }

    @Test
    fun `all example files compile through full pipeline`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found in examples/ directory")

        val errors = mutableListOf<String>()
        for ((name, source) in examples) {
            val outputDir = File(System.getProperty("java.io.tmpdir"), "regression-${name}-${System.nanoTime()}")
            outputDir.mkdirs()
            try {
                val cleanSource = stripExpectedComments(source)
                val result = pipeline().compileProject(
                    sourceFiles = listOf(SourceInput("$name.has", cleanSource)),
                    outputDir = outputDir,
                )
                if (!result.success) {
                    val errorDetail = buildString {
                        appendLine("  Compile errors: ${result.compileErrors.joinToString("; ") { it.message }}")
                        if (result.javaErrors.isNotEmpty()) {
                            appendLine("  Java errors: ${result.javaErrors.joinToString("; ") { it.message }}")
                        }
                    }
                    errors.add("$name$errorDetail")
                }
            } finally {
                outputDir.deleteRecursively()
            }
        }

        if (errors.isNotEmpty()) {
            assertTrue(false, "The following examples failed to compile:\n${errors.joinToString("\n")}")
        }
    }

    @Test
    fun `all example files pass semantic analysis`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found in examples/ directory")

        val errors = mutableListOf<String>()
        for ((name, source) in examples) {
            val cleanSource = stripExpectedComments(source)
            val sf = SourceFile("$name.has", cleanSource)
            val lexerResult = Lexer(sf).tokenize()
            val parseResult = Parser(lexerResult).parse()
            val semanticResult = SemanticAnalyzer().analyze(parseResult.module)

            if (semanticResult.hasErrors) {
                errors.add("$name: ${semanticResult.errors.joinToString("; ") { it.message }}")
            }
        }

        if (errors.isNotEmpty()) {
            assertTrue(false, "Semantic analysis failed:\n${errors.joinToString("\n")}")
        }
    }

    @Test
    fun `all example files pass type checking`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found in examples/ directory")

        val errors = mutableListOf<String>()
        for ((name, source) in examples) {
            val cleanSource = stripExpectedComments(source)
            val sf = SourceFile("$name.has", cleanSource)
            val lexerResult = Lexer(sf).tokenize()
            val parseResult = Parser(lexerResult).parse()
            val typeResult = TypeChecker().check(parseResult.module)

            if (typeResult.hasErrors) {
                errors.add("$name: ${typeResult.diagnostics.joinToString("; ") { it.message }}")
            }
        }

        if (errors.isNotEmpty()) {
            assertTrue(false, "Type checking failed:\n${errors.joinToString("\n")}")
        }
    }

    @Test
    fun `all example files generate valid Java source`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found in examples/ directory")

        val errors = mutableListOf<String>()
        for ((name, source) in examples) {
            val cleanSource = stripExpectedComments(source)
            val sf = SourceFile("$name.has", cleanSource)
            val lexerResult = Lexer(sf).tokenize()
            val parseResult = Parser(lexerResult).parse()
            val typeResult = TypeChecker().check(parseResult.module)
            val generator = JavaSourceGenerator(typeResult.diagnostics)
            val javaSource = generator.generate(parseResult.module)

            if (javaSource.isBlank()) {
                errors.add("$name: Generated Java source is empty")
            }
        }

        if (errors.isNotEmpty()) {
            assertTrue(false, "Java source generation failed:\n${errors.joinToString("\n")}")
        }
    }

    @Test
    fun `at least 20 example files exist`() {
        val examples = discoverExamples()
        assertTrue(
            examples.size >= 20,
            "Expected at least 20 example files, found ${examples.size}"
        )
    }

    @Test
    fun `HasabToJavaCompiler produces same results for each example`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found in examples/ directory")

        for ((name, source) in examples) {
            val cleanSource = stripExpectedComments(source)
            val result = HasabToJavaCompiler.compile(cleanSource, "$name.has")
            assertTrue(
                result.javaSource.isNotBlank(),
                "$name: HasabToJavaCompiler produced empty source"
            )
        }
    }
}
