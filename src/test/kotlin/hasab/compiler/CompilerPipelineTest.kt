package hasab.compiler

import hasab.compiler.backend.BackendType
import hasab.compiler.backend.SourceMap
import hasab.compiler.optimizer.OptProfile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompilerPipelineTest {

    private fun pipeline(): CompilerPipeline = CompilerPipeline(
        config = PipelineConfig(backendType = BackendType.JAVA_SOURCE, optProfile = OptProfile.Debug)
    )

    private fun createTempDir(prefix: String): File {
        val dir = File(System.getProperty("java.io.tmpdir"), "${prefix}-${System.nanoTime()}")
        dir.mkdirs()
        return dir
    }

    @Test
    fun `compile function-only program through javac`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", """
                    fn add(a: int, b: int) -> int { return a + b; }
                    fn main() { let x: int = add(1, 2); }
                """.trimIndent())),
                outputDir = outputDir,
            )
            assertTrue(result.success, "Expected success, compileErrors=${result.compileErrors}, javaErrors=${result.javaErrors}")
            assertTrue(result.generatedSources.isNotEmpty())
            assertTrue(result.mainClassName.isNotEmpty())
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile struct and function program through javac`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", """
                    struct Point { x: int, y: int }
                    fn getX(p: Point) -> int { return p.x; }
                    fn main() { let v: int = getX(Point(1, 2)); }
                """.trimIndent())),
                outputDir = outputDir,
            )
            assertTrue(result.success, "Expected success, compileErrors=${result.compileErrors}, javaErrors=${result.javaErrors}")
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile enum program through javac`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", """
                    enum Color { Red, Green, Blue }
                    fn main() {
                        let c: int = 1;
                    }
                """.trimIndent())),
                outputDir = outputDir,
            )
            assertTrue(result.success, "Expected success, compileErrors=${result.compileErrors}, javaErrors=${result.javaErrors}")
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile with syntax error reports errors`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", "fn {")),
                outputDir = outputDir,
            )
            assertFalse(result.success)
            assertTrue(result.compileErrors.isNotEmpty())
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile with type error reports errors`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", "fn add(x: int) -> int { return x + true; }")),
                outputDir = outputDir,
            )
            assertFalse(result.success)
            assertTrue(result.compileErrors.isNotEmpty())
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile produces source map`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", "struct Empty { }")),
                outputDir = outputDir,
            )
            assertTrue(result.success, "Expected success, javaErrors=${result.javaErrors}")
            assertNotNull(result.sourceMap)
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile produces main class name`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(SourceInput("main.has", "struct App { name: string }")),
                outputDir = outputDir,
            )
            assertTrue(result.success, "Expected success, javaErrors=${result.javaErrors}")
            assertTrue(result.mainClassName.isNotEmpty())
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `compile multiple source files`() {
        val outputDir = createTempDir("pipeline-test")
        try {
            val result = pipeline().compileProject(
                sourceFiles = listOf(
                    SourceInput("utils.has", "struct Utils { value: int }"),
                    SourceInput("main.has", "struct App { name: string }"),
                ),
                outputDir = outputDir,
            )
            assertEquals(2, result.modules.size)
            assertTrue(result.success, "Expected success, compileErrors=${result.compileErrors}, javaErrors=${result.javaErrors}")
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `pipeline config default values`() {
        val config = PipelineConfig()
        assertEquals(BackendType.JAVA_SOURCE, config.backendType)
        assertEquals(OptProfile.Debug, config.optProfile)
    }

    @Test
    fun `source input data class`() {
        val input = SourceInput("main.has", "fn main() { }")
        assertEquals("main.has", input.fileName)
        assertEquals("fn main() { }", input.sourceCode)
    }

    @Test
    fun `pipeline result data class`() {
        val sm = SourceMap()
        val result = PipelineResult(
            success = true,
            generatedSources = mapOf("Main.java" to "class Main {}"),
            mainClassName = "Main",
            sourceMap = sm,
            compileErrors = emptyList(),
            javaErrors = emptyList(),
            modules = emptyList(),
        )
        assertTrue(result.success)
        assertEquals("Main", result.mainClassName)
        assertEquals(1, result.generatedSources.size)
    }

    @Test
    fun `source mapped error data class`() {
        val err = SourceMappedError("main.has", 5, 2, "type mismatch", "error", "Main.java", 10)
        assertEquals("main.has", err.sourceFile)
        assertEquals(5, err.sourceLine)
        assertEquals(2, err.sourceColumn)
        assertEquals("type mismatch", err.message)
        assertEquals("error", err.severity)
        assertEquals("Main.java", err.generatedFile)
        assertEquals(10, err.generatedLine)
    }

    @Test
    fun `compile from directory with no has files fails`() {
        val tmpDir = createTempDir("pipeline-dir-test")
        try {
            val result = pipeline().compileProjectFromDirectory(tmpDir, tmpDir, tmpDir)
            assertFalse(result.success)
            assertTrue(result.compileErrors.isNotEmpty())
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun `compile from directory with has files`() {
        val projectDir = createTempDir("pipeline-dir-test")
        val srcDir = File(projectDir, "src")
        srcDir.mkdirs()
        val outDir = File(projectDir, "build")
        try {
            File(srcDir, "main.has").writeText("struct App { name: string }")
            val result = pipeline().compileProjectFromDirectory(projectDir, srcDir, outDir)
            assertTrue(result.success, "Expected success, compileErrors=${result.compileErrors}, javaErrors=${result.javaErrors}")
            assertTrue(result.generatedSources.isNotEmpty())
        } finally {
            projectDir.deleteRecursively()
        }
    }
}
