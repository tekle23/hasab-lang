package hasab.compiler.backend.javac

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JavacInvokerTest {

    @Test
    fun `parse javac error format`() {
        val invoker = JavacInvoker()
        val errors = invokeParseJavacErrors(invoker, "Main.java:5:2: error: cannot find symbol")
        assertEquals(1, errors.size)
        assertEquals("Main.java", errors[0].file)
        assertEquals(5, errors[0].line)
        assertEquals(2, errors[0].column)
        assertEquals("cannot find symbol", errors[0].message)
        assertEquals("error", errors[0].severity)
    }

    @Test
    fun `parse javac warning format`() {
        val invoker = JavacInvoker()
        val errors = invokeParseJavacErrors(invoker, "Main.java:10: warning: unused variable")
        assertEquals(1, errors.size)
        assertEquals("warning", errors[0].severity)
    }

    @Test
    fun `parse multiple javac errors`() {
        val invoker = JavacInvoker()
        val output = """
            Main.java:5:2: error: cannot find symbol
            Main.java:10:1: error: method does not override
            Main.java:15:3: warning: unchecked cast
        """.trimIndent()
        val errors = invokeParseJavacErrors(invoker, output)
        assertEquals(3, errors.size)
        assertEquals("error", errors[0].severity)
        assertEquals("error", errors[1].severity)
        assertEquals("warning", errors[2].severity)
    }

    @Test
    fun `parse non-standard output as raw messages`() {
        val invoker = JavacInvoker()
        val errors = invokeParseJavacErrors(invoker, "some random output")
        assertEquals(1, errors.size)
        assertEquals("some random output", errors[0].message)
    }

    @Test
    fun `parse empty output returns empty list`() {
        val invoker = JavacInvoker()
        val errors = invokeParseJavacErrors(invoker, "")
        assertEquals(0, errors.size)
    }

    @Test
    fun `compile directory with no java files fails`() {
        val invoker = JavacInvoker()
        val tmpDir = createTempDir("javac-test")
        try {
            val result = invoker.compileDirectory(tmpDir, tmpDir)
            assertFalse(result.success)
            assertTrue(result.errors.isNotEmpty())
            assertTrue(result.errors[0].message.contains("No .java files"))
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun `compile with valid java file succeeds`() {
        val invoker = JavacInvoker()
        val srcDir = createTempDir("javac-test-src")
        val classesDir = createTempDir("javac-test-classes")
        try {
            File(srcDir, "Hello.java").writeText(
                """
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello");
                    }
                }
                """.trimIndent()
            )

            val result = invoker.compileDirectory(srcDir, classesDir)
            assertTrue(result.success, "javac should succeed, errors: ${result.errors}")
            assertTrue(File(classesDir, "Hello.class").exists(), "Hello.class should exist")
        } finally {
            srcDir.deleteRecursively()
            classesDir.deleteRecursively()
        }
    }

    @Test
    fun `compile with invalid java file fails`() {
        val invoker = JavacInvoker()
        val srcDir = createTempDir("javac-test-src")
        val classesDir = createTempDir("javac-test-classes")
        try {
            File(srcDir, "Bad.java").writeText(
                """
                public class Bad {
                    public static void main(String[] args) {
                        System.out.println(undefinedVariable);
                    }
                }
                """.trimIndent()
            )

            val result = invoker.compileDirectory(srcDir, classesDir)
            assertFalse(result.success)
            assertTrue(result.errors.any { it.severity == "error" })
        } finally {
            srcDir.deleteRecursively()
            classesDir.deleteRecursively()
        }
    }

    @Test
    fun `javacInvoker data classes`() {
        val err = JavacError("Main.java", 5, 2, "error msg", "error")
        assertEquals("Main.java", err.file)
        assertEquals(5, err.line)
        assertEquals(2, err.column)
        assertEquals("error msg", err.message)
        assertEquals("error", err.severity)
    }

    private fun invokeParseJavacErrors(invoker: JavacInvoker, output: String): List<JavacError> {
        val method = JavacInvoker::class.java.getDeclaredMethod("parseJavacErrors", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(invoker, output) as List<JavacError>
    }
}
