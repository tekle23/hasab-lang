package hasab.compiler

import hasab.compiler.backend.JavaSourceGenerator
import hasab.compiler.frontend.lexer.Keyword
import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.semantic.SemanticAnalyzer
import hasab.compiler.types.TypeChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * End-to-end tests verifying that English and Amharic builtin calls
 * produce identical ASTs and identical Java output.
 */
class BilingualE2ETest {

    private fun amharicFor(latin: String): String {
        val kw = Keyword.lookup(latin) ?: error("No keyword for '$latin'")
        return String(kw.amharicChars)
    }

    private fun compileToJava(source: String): String {
        val file = SourceFile("test.has", source)
        val lexerResult = Lexer(file).tokenize()
        val parseResult = Parser(lexerResult).parse()
        assertFalse(parseResult.hasErrors, "Parse errors: ${parseResult.diagnostics.joinToString("; ") { it.message }}")

        val semanticResult = SemanticAnalyzer().analyze(parseResult.module)
        assertFalse(semanticResult.hasErrors, "Semantic errors: ${semanticResult.errors.joinToString("; ") { it.message }}")

        val typeResult = TypeChecker().check(parseResult.module)
        assertFalse(typeResult.hasErrors, "Type errors: ${typeResult.diagnostics.joinToString("; ") { it.message }}")

        val generator = JavaSourceGenerator(typeResult.diagnostics)
        return generator.generate(parseResult.module)
    }

    @Test
    fun `println and amharic println produce identical Java output`() {
        val amharic = amharicFor("println")
        val latinCode = """
            fn main() {
                println("hello");
            }
        """.trimIndent()
        val amharicCode = """
            fn main() {
                $amharic("hello");
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
    }

    @Test
    fun `println and amharic println with variable produce identical Java`() {
        val amharic = amharicFor("println")
        val latinCode = """
            fn main() {
                let name = "HASAB";
                println(name);
            }
        """.trimIndent()
        val amharicCode = """
            fn main() {
                let name = "HASAB";
                $amharic(name);
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
    }

    @Test
    fun `fully bilingual program compiles to same Java as English`() {
        val fnKw = amharicFor("fn")
        val letKw = amharicFor("let")
        val returnKw = amharicFor("return")
        val printlnKw = amharicFor("println")

        val latinCode = """
            fn add(a: int, b: int) -> int {
                return a + b;
            }
            fn main() {
                let result = add(2, 3);
                println(result);
            }
        """.trimIndent()
        val amharicCode = """
            $fnKw add(a: int, b: int) -> int {
                $returnKw a + b;
            }
            $fnKw main() {
                $letKw result = add(2, 3);
                $printlnKw(result);
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
    }

    @Test
    fun `main and amharic wäna produce identical Java output`() {
        val fnKw = amharicFor("fn")
        val mainKw = amharicFor("main")
        
        val latinCode = """
            fn main() {
                println("hello");
            }
        """.trimIndent()
        val amharicCode = """
            $fnKw $mainKw() {
                println("hello");
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
        assertTrue(latinJava.contains("public static void main(String[] args)"), 
            "Generated Java should contain 'public static void main(String[] args)'")
    }

    @Test
    fun `main and amharic wäna with parameters produce identical Java`() {
        val fnKw = amharicFor("fn")
        val mainKw = amharicFor("main")
        val letKw = amharicFor("let")
        
        val latinCode = """
            fn main() {
                let name = "HASAB";
                println(name);
            }
        """.trimIndent()
        val amharicCode = """
            $fnKw $mainKw() {
                $letKw name = "HASAB";
                println(name);
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
        assertTrue(latinJava.contains("public static void main(String[] args)"), 
            "Generated Java should contain 'public static void main(String[] args)'")
    }

    @Test
    fun `fully bilingual program with amharic main compiles to same Java as English`() {
        val fnKw = amharicFor("fn")
        val letKw = amharicFor("let")
        val returnKw = amharicFor("return")
        val printlnKw = amharicFor("println")
        val mainKw = amharicFor("main")

        val latinCode = """
            fn add(a: int, b: int) -> int {
                return a + b;
            }
            fn main() {
                let result = add(2, 3);
                println(result);
            }
        """.trimIndent()
        val amharicCode = """
            $fnKw add(a: int, b: int) -> int {
                $returnKw a + b;
            }
            $fnKw $mainKw() {
                $letKw result = add(2, 3);
                $printlnKw(result);
            }
        """.trimIndent()

        val latinJava = compileToJava(latinCode)
        val amharicJava = compileToJava(amharicCode)

        assertEquals(latinJava, amharicJava)
        assertTrue(latinJava.contains("public static void main(String[] args)"), 
            "Generated Java should contain 'public static void main(String[] args)'")
    }
}
