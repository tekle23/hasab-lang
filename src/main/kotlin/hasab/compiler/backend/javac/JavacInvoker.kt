package hasab.compiler.backend.javac

import java.io.File
import java.nio.file.Path

public data class JavacResult(
    val success: Boolean,
    val errors: List<JavacError>,
    val classesDir: File,
)

public data class JavacError(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val severity: String = "error",
)

public class JavacInvoker {

    public fun compile(
        javaFiles: List<File>,
        classesDir: File,
        classpath: String? = null,
        sourcePath: File? = null,
    ): JavacResult {
        classesDir.mkdirs()

        val javacPath = findJavac()
            ?: return JavacResult(
                success = false,
                errors = listOf(JavacError("", 0, 0, "javac not found. Ensure JDK is installed and on PATH.")),
                classesDir = classesDir,
            )

        val args = mutableListOf<String>()
        args.add("-d")
        args.add(classesDir.absolutePath)
        args.add("-g")
        args.add("-source")
        args.add("21")
        args.add("-target")
        args.add("21")

        if (classpath != null) {
            args.add("-classpath")
            args.add(classpath)
        }
        if (sourcePath != null) {
            args.add("-sourcepath")
            args.add(sourcePath.absolutePath)
        }

        args.addAll(javaFiles.map { it.absolutePath })

        val processBuilder = ProcessBuilder(javacPath, *args.toTypedArray())
        processBuilder.redirectErrorStream(false)

        return try {
            val process = processBuilder.start()
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                JavacResult(success = true, errors = emptyList(), classesDir = classesDir)
            } else {
                val errors = parseJavacErrors(stdout + "\n" + stderr)
                JavacResult(success = false, errors = errors, classesDir = classesDir)
            }
        } catch (e: Exception) {
            JavacResult(
                success = false,
                errors = listOf(JavacError("", 0, 0, "Failed to run javac: ${e.message}")),
                classesDir = classesDir,
            )
        }
    }

    public fun compileDirectory(
        sourceDir: File,
        classesDir: File,
        classpath: String? = null,
    ): JavacResult {
        val javaFiles = sourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "java" }
            .toList()

        if (javaFiles.isEmpty()) {
            return JavacResult(
                success = false,
                errors = listOf(JavacError("", 0, 0, "No .java files found in ${sourceDir.path}")),
                classesDir = classesDir,
            )
        }

        return compile(javaFiles, classesDir, classpath, sourceDir)
    }

    private fun findJavac(): String? {
        val javaHome = System.getProperty("java.home") ?: return null
        val jdkHome = if (javaHome.endsWith("jre") || javaHome.endsWith("\\jre") || javaHome.endsWith("/jre")) {
            File(javaHome).parent
        } else {
            javaHome
        }

        val candidates = listOf(
            File(jdkHome, "bin/javac.exe"),
            File(jdkHome, "bin/javac"),
            File(System.getProperty("java.home"), "../bin/javac.exe"),
            File(System.getProperty("java.home"), "../bin/javac"),
        )

        for (candidate in candidates) {
            val normalized = candidate.normalize()
            if (normalized.exists()) {
                return normalized.absolutePath
            }
        }

        return try {
            val process = ProcessBuilder("where", "javac")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            if (output.isNotEmpty()) output.lines().first() else null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseJavacErrors(output: String): List<JavacError> {
        val errors = mutableListOf<JavacError>()
        val pattern = Regex("""^(.+?):(\d+)(?::(\d+))?\s*:\s*(error|warning)\s*:\s*(.+)$""")

        for (line in output.lines()) {
            val match = pattern.matchEntire(line.trim())
            if (match != null) {
                val file = match.groupValues[1]
                val lineNum = match.groupValues[2].toIntOrNull() ?: 0
                val colNum = match.groupValues[3].toIntOrNull() ?: 0
                val severity = match.groupValues[4]
                val message = match.groupValues[5]
                errors.add(JavacError(file, lineNum, colNum, message, severity))
            }
        }

        if (errors.isEmpty() && output.isNotBlank()) {
            for (line in output.lines()) {
                if (line.isNotBlank()) {
                    errors.add(JavacError("", 0, 0, line))
                }
            }
        }

        return errors
    }
}
