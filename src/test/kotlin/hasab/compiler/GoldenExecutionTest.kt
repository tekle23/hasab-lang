package hasab.compiler

import hasab.compiler.backend.BackendType
import hasab.compiler.backend.javac.JavacInvoker
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.optimizer.OptProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.util.concurrent.TimeUnit

class GoldenExecutionTest {

    private val javacInvoker = JavacInvoker()

    private fun pipeline(): CompilerPipeline = CompilerPipeline(
        config = PipelineConfig(backendType = BackendType.JAVA_SOURCE, optProfile = OptProfile.Debug)
    )

    private fun examplesDir(): File = File(System.getProperty("user.dir"), "examples")

    private fun goldenDir(): File = File(File(System.getProperty("user.dir"), "examples"), "golden")

    private fun discoverExamples(): List<Pair<String, String>> {
        val dir = examplesDir()
        if (!dir.exists()) return emptyList()
        return dir.listFiles()
            ?.filter { it.isFile && it.extension == "has" && !it.parentFile.name.equals("golden", ignoreCase = true) }
            ?.sortedBy { it.name }
            ?.map { it.nameWithoutExtension to it.readText() }
            ?: emptyList()
    }

    private fun stripExpectedComments(source: String): String {
        return source.lines()
            .filter { !it.trimStart().startsWith("// EXPECTED:") }
            .joinToString("\n")
    }

    private fun normalizeLineEndings(s: String): String = s.replace("\r\n", "\n").trimEnd()

    private fun normalizeOutput(s: String): String {
        return normalizeLineEndings(s).lines().joinToString("\n") { line ->
            if (line.trim().toLongOrNull() != null) "<TIMESTAMP>" else line
        }
    }

    private fun compileAndRun(name: String, source: String): Pair<Boolean, String> {
        val outputDir = File(System.getProperty("java.io.tmpdir"), "golden-$name-${System.nanoTime()}")
        val classesDir = File(outputDir, "classes")
        classesDir.mkdirs()

        try {
            val cleanSource = stripExpectedComments(source)
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("$name.has", cleanSource)),
                outputDir = outputDir,
            )

            if (!result.success) {
                val errors = buildString {
                    result.compileErrors.forEach { appendLine("  compile: ${it.message}") }
                    result.javaErrors.forEach { appendLine("  java: ${it.message}") }
                }
                return false to "Compilation failed:\n$errors"
            }

            val javacResult = javacInvoker.compileDirectory(classesDir, classesDir)
            if (!javacResult.success) {
                val errors = javacResult.errors.joinToString("; ") { it.message }
                return false to "Javac failed: $errors"
            }

            val className = result.mainClassName.ifEmpty {
                result.generatedSources.keys.first().removeSuffix(".java")
            }

            val processBuilder = ProcessBuilder(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                "-cp", classesDir.absolutePath,
                className,
            )
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()
            val stdout = process.inputStream.bufferedReader().readText()
            val exited = process.waitFor(10, TimeUnit.SECONDS)

            if (!exited) {
                process.destroyForcibly()
                return false to "Execution timed out (10s)"
            }

            if (process.exitValue() != 0) {
                return false to "Execution failed (exit ${process.exitValue()}):\n$stdout"
            }

            return true to stdout
        } catch (e: Exception) {
            return false to "Exception: ${e.message}"
        } finally {
            outputDir.deleteRecursively()
        }
    }

    private fun expectedOutput(name: String): String? {
        val goldenFile = File(goldenDir(), "$name.expected")
        return if (goldenFile.exists()) goldenFile.readText() else null
    }

    private fun saveExpectedOutput(name: String, output: String) {
        val dir = goldenDir()
        dir.mkdirs()
        File(dir, "$name.expected").writeText(normalizeOutput(output))
    }

    @Test
    fun `generate golden baselines for all examples`() {
        val examples = discoverExamples()
        assertTrue(examples.isNotEmpty(), "No .has files found")

        val report = mutableListOf<String>()
        for ((name, source) in examples) {
            val (success, output) = compileAndRun(name, source)
            if (success) {
                saveExpectedOutput(name, output)
                report.add("OK: $name -> ${output.lines().size} lines")
            } else {
                report.add("SKIP: $name -> $output")
            }
        }
        println("Golden baseline generation report:\n${report.joinToString("\n")}")

        val generated = goldenDir().listFiles()?.filter { it.extension == "expected" }?.size ?: 0
        assertTrue(generated > 0, "No golden baselines generated")
    }

    @Test
    fun `all examples produce expected output`() {
        val examples = discoverExamples()
        val goldenDir = goldenDir()
        assertTrue(goldenDir.exists() && goldenDir.listFiles()?.isNotEmpty() == true,
            "Golden baselines not found. Run 'generate golden baselines' first.")

        val failures = mutableListOf<String>()
        for ((name, source) in examples) {
            val expected = expectedOutput(name) ?: continue

            val (success, actual) = compileAndRun(name, source)
            if (!success) {
                failures.add("$name: failed to compile/run: $actual")
                continue
            }

            if (normalizeOutput(actual) != normalizeOutput(expected)) {
                failures.add("$name: output mismatch\n  expected: ${expected.lines().firstOrNull()}\n  actual:   ${actual.lines().firstOrNull()}")
            }
        }

        if (failures.isNotEmpty()) {
            assertTrue(false, "Golden test failures:\n${failures.joinToString("\n")}")
        }
    }
}
