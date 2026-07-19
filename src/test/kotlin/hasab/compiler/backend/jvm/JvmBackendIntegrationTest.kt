package hasab.compiler.backend.jvm

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import hasab.compiler.hir.AstToHirLowering
import hasab.compiler.hir.HirFnDecl
import hasab.compiler.hir.HirStructDecl
import hasab.compiler.hir.HirEnumDecl
import hasab.compiler.hir.HirField
import hasab.compiler.hir.HirEnumVariant
import hasab.compiler.hir.cfg.*
import hasab.compiler.types.IntType
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JvmBackendIntegrationTest {

    private fun compileToJvm(code: String, className: String = "Test"): JvmCompilationResult {
        val source = SourceFile("test.hasab", code)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()
        val typeChecker = TypeChecker()
        val typeCheckResult = typeChecker.check(parseResult.module)
        val lowering = AstToHirLowering(typeCheckResult.environment)
        val hirModule = lowering.lower(parseResult.module)
        val cfgFunctions = mutableMapOf<String, HirCfgFunction>()
        for (decl in hirModule.declarations) {
            if (decl is HirFnDecl) {
                cfgFunctions[decl.name] = CfgBuilder().build(decl)
            }
        }
        val cfgModule = HirCfgModule(className, cfgFunctions)
        val backend = JvmBackend(JvmBackendConfig(className = className, validate = true))
        return backend.compile(cfgModule)
    }

    private fun compileAndLoad(code: String, className: String = "Test"): Class<*> {
        val result = compileToJvm(code, className)
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        val classBytes = result.classes[className]
            ?: throw AssertionError("No class $className in output")
        val loader = object : ClassLoader(this::class.java.classLoader) {
            fun load(name: String, bytes: ByteArray): Class<*> {
                return defineClass(name, bytes, 0, bytes.size)
            }
        }
        return loader.load(className, classBytes)
    }

    @Test
    fun `simple void function compiles`() {
        val result = compileToJvm("fn main() { }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with int return compiles`() {
        val result = compileToJvm("fn add(x: int, y: int) -> int { return x + y; }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with string param compiles`() {
        val result = compileToJvm("fn greet(name: string) { println(name); }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with binary operations compiles`() {
        val result = compileToJvm("fn calc() -> int { let z = 3 + 4; return z; }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with let statement compiles`() {
        val result = compileToJvm("fn main() { let x = 42; }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with if statement compiles`() {
        val result = compileToJvm("fn main() { if true { let x = 1; } }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with while loop compiles`() {
        val result = compileToJvm("fn main() { while true { } }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `multiple functions compile`() {
        val code = """
            fn add(x: int, y: int) -> int { return x + y; }
            fn main() { }
        """.trimIndent()
        val result = compileToJvm(code)
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `println call compiles`() {
        val result = compileToJvm("""fn main() { println("hello"); }""")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `function with return value and operations compiles`() {
        val result = compileToJvm("fn double(n: int) -> int { return n * 2; }")
        assertTrue(result.success, "Compilation failed: ${result.diagnostics}")
        assertTrue(result.classes.containsKey("Test"))
    }

    @Test
    fun `compiled class has correct methods`() {
        val code = """
            fn add(x: int, y: int) -> int { return x + y; }
            fn main() { }
        """.trimIndent()
        val clazz = compileAndLoad(code)
        val methods = clazz.declaredMethods
        val methodNames = methods.map { it.name }.toSet()
        assertTrue("add" in methodNames, "Expected 'add' method, got: $methodNames")
        assertTrue("main" in methodNames, "Expected 'main' method, got: $methodNames")
    }

    @Test
    fun `result has no diagnostics for valid input`() {
        val result = compileToJvm("fn main() { }")
        assertTrue(result.success)
        assertEquals(0, result.diagnostics.size, "Expected no diagnostics but got: ${result.diagnostics}")
    }

    @Test
    fun `struct class generates`() {
        val structDecl = HirStructDecl(
            name = "Point",
            fields = listOf(
                HirField("x", IntType, false),
                HirField("y", IntType, false),
            ),
            isPublic = false,
        )
        val backend = JvmBackend(JvmBackendConfig())
        val bytes = backend.generateStructClass(structDecl)
        assertTrue(bytes.isNotEmpty())
        val magic = ((bytes[0].toInt() and 0xFF).toLong() shl 24) or
            ((bytes[1].toInt() and 0xFF).toLong() shl 16) or
            ((bytes[2].toInt() and 0xFF).toLong() shl 8) or
            (bytes[3].toInt() and 0xFF).toLong()
        assertEquals(0xCAFEBABEL, magic)
    }

    @Test
    fun `enum class generates`() {
        val enumDecl = HirEnumDecl(
            name = "Color",
            variants = listOf(
                HirEnumVariant("Red", emptyList()),
                HirEnumVariant("Green", emptyList()),
                HirEnumVariant("Blue", emptyList()),
            ),
            isPublic = false,
        )
        val backend = JvmBackend(JvmBackendConfig())
        val bytes = backend.generateEnumClass(enumDecl)
        assertTrue(bytes.isNotEmpty())
    }
}
