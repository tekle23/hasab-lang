package hasab.compiler

import hasab.compiler.backend.BackendType
import hasab.compiler.backend.HasabToJavaCompiler
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.types.TypeChecker
import hasab.compiler.backend.JavaSourceGenerator
import hasab.compiler.optimizer.OptProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class CompilerBenchmarkTest {

    private fun readExample(name: String): String {
        val file = File(System.getProperty("user.dir"), "examples/$name.has")
        return if (file.exists()) file.readText() else ""
    }

    private fun readAllExamples(): Map<String, String> {
        val dir = File(System.getProperty("user.dir"), "examples")
        return dir.listFiles()
            ?.filter { it.isFile && it.extension == "has" }
            ?.sortedBy { it.name }
            ?.associate { it.nameWithoutExtension to it.readText() }
            ?: emptyMap()
    }

    private fun bench(label: String, iterations: Int = 100, block: () -> Unit): Double {
        repeat(5) { block() }
        val times = (1..iterations).map {
            val start = System.nanoTime()
            block()
            val elapsed = System.nanoTime() - start
            elapsed / 1_000_000.0
        }
        val avg = times.average()
        val p95 = times.sorted()[(times.size * 0.95).toInt().coerceAtMost(times.size - 1)]
        println("  $label: avg=%.2fms  p95=%.2fms  min=%.2fms  max=%.2fms".format(avg, p95, times.min(), times.max()))
        return avg
    }

    @Test
    fun `compiler pipeline benchmark`() {
        println("=== HASAB Compiler Performance Benchmark ===")
        println()

        val examples = readAllExamples()
        assertTrue(examples.isNotEmpty(), "No examples found")

        val allSource = examples.values.joinToString("\n")
        val largeSource = allSource.repeat(10)

        println("--- Single Example Compilation (small: 01_hello_world) ---")
        val small = examples["01_hello_world"] ?: ""
        bench("Lexer (1x)", 200) { Lexer(SourceFile("test.has", small)).tokenize() }

        val lexerResult = Lexer(SourceFile("test.has", small)).tokenize()
        bench("Parser (1x)", 200) { Parser(lexerResult).parse() }

        val parseResult = Parser(lexerResult).parse()
        bench("SemanticAnalyzer (1x)", 200) { SemanticAnalyzer().analyze(parseResult.module) }

        val semanticResult = SemanticAnalyzer().analyze(parseResult.module)
        bench("TypeChecker (1x)", 200) { TypeChecker().check(parseResult.module) }

        val typeResult = TypeChecker().check(parseResult.module)
        bench("JavaSourceGenerator (1x)", 200) { JavaSourceGenerator(typeResult.diagnostics).generate(parseResult.module) }

        println()
        println("--- Full Pipeline: all 27 examples (serial) ---")
        bench("Full pipeline (27 examples)", 50) {
            for ((name, source) in examples) {
                HasabToJavaCompiler.compile(source, "$name.has")
            }
        }

        println()
        println("--- Large Source Compilation (10x all examples) ---")
        bench("Lexer (10x all)", 20) {
            Lexer(SourceFile("large.has", largeSource)).tokenize()
        }

        val largeLexer = Lexer(SourceFile("large.has", largeSource)).tokenize()
        bench("Parser (10x all)", 20) {
            Parser(largeLexer).parse()
        }

        println()
        println("--- Memory Usage ---")
        val runtime = Runtime.getRuntime()
        runtime.gc()
        val beforeMem = runtime.totalMemory() - runtime.freeMemory()
        for ((name, source) in examples) {
            HasabToJavaCompiler.compile(source, "$name.has")
        }
        runtime.gc()
        val afterMem = runtime.totalMemory() - runtime.freeMemory()
        println("  Memory before: ${beforeMem / 1024}KB")
        println("  Memory after:  ${afterMem / 1024}KB")
        println("  Delta:         ${(afterMem - beforeMem) / 1024}KB")
        println()

        println("--- Pipeline Result Summary ---")
        var success = 0
        var failed = 0
        val pipeline = CompilerPipeline(
            config = PipelineConfig(backendType = BackendType.JAVA_SOURCE, optProfile = OptProfile.Debug)
        )
        for ((name, source) in examples) {
            val outputDir = File(System.getProperty("java.io.tmpdir"), "bench-$name-${System.nanoTime()}")
            outputDir.mkdirs()
            try {
                val result = pipeline.compileProject(
                    sourceFiles = listOf(SourceInput("$name.has", source)),
                    outputDir = outputDir,
                )
                if (result.success) success++ else failed++
            } finally {
                outputDir.deleteRecursively()
            }
        }
        println("  Examples: ${examples.size}")
        println("  Pipeline success: $success")
        println("  Pipeline failures: $failed")

        assertTrue(true, "Benchmark completed")
    }
}
