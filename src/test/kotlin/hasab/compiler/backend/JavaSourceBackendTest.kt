package hasab.compiler.backend

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JavaSourceBackendTest {

    private fun compileModule(code: String, fileName: String = "test.has"): CompiledModule {
        val source = SourceFile(fileName, code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val tc = TypeChecker()
        val result = tc.check(parseResult.module)
        return CompiledModule(fileName = fileName, ast = parseResult.module, typeCheckResult = result)
    }

    @Test
    fun `generate produces java source`() {
        val module = compileModule("fn add(x: int, y: int) -> int { return x + y; }")
        val backend = JavaSourceBackend(JavaSourceGenerator())
        val context = BackendContext(modules = listOf(module), sourceMap = SourceMap())
        val output = backend.generate(context)

        assertTrue(output.generatedSources.isNotEmpty())
        assertTrue(output.generatedSources.values.first().contains("add"))
    }

    @Test
    fun `generate maps class name to source file`() {
        val module = compileModule("fn noop() { }")
        val sourceMap = SourceMap()
        val backend = JavaSourceBackend(JavaSourceGenerator())
        val context = BackendContext(modules = listOf(module), sourceMap = sourceMap)
        val output = backend.generate(context)

        assertEquals(1, output.generatedSources.size)
        val javaFile = output.generatedSources.keys.first()
        assertTrue(javaFile.endsWith(".java"))
    }

    @Test
    fun `backend type is JAVA_SOURCE`() {
        val backend = JavaSourceBackend(JavaSourceGenerator())
        assertEquals(BackendType.JAVA_SOURCE, backend.backendType)
    }

    @Test
    fun `main class name is set for file with main function`() {
        val module = compileModule("fn main() { }")
        val backend = JavaSourceBackend(JavaSourceGenerator())
        val context = BackendContext(modules = listOf(module), sourceMap = SourceMap())
        val output = backend.generate(context)

        assertTrue(output.mainClassName.isNotEmpty())
    }

    @Test
    fun `main class name falls back to first file`() {
        val module = compileModule("fn helper() -> int { return 42; }")
        val backend = JavaSourceBackend(JavaSourceGenerator())
        val context = BackendContext(modules = listOf(module), sourceMap = SourceMap())
        val output = backend.generate(context)

        assertTrue(output.mainClassName.isNotEmpty())
    }

    @Test
    fun `multiple modules produce multiple generated sources`() {
        val mod1 = compileModule("fn helper() -> int { return 42; }", "utils.has")
        val mod2 = compileModule("fn main() { }", "main.has")
        val backend = JavaSourceBackend(JavaSourceGenerator())
        val context = BackendContext(modules = listOf(mod1, mod2), sourceMap = SourceMap())
        val output = backend.generate(context)

        assertEquals(2, output.generatedSources.size)
    }
}
