package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.Terminal
import hasab.cli.config.ProjectConfig
import hasab.compiler.CompilerPipeline
import hasab.compiler.PipelineConfig
import hasab.compiler.PipelineResult
import hasab.compiler.backend.BackendType
import hasab.compiler.backend.SourceMap
import hasab.compiler.backend.javac.JavacInvoker
import hasab.compiler.optimizer.OptProfile
import java.io.File

/**
 * Builds and executes the current HASAB project.
 *
 * Pipeline: source → lex → parse → semantic → typecheck → HIR → optimize → Java → javac → JVM
 */
public class RunCommand : Command {
    override val name: String = "run"
    override val description: String = "Build and run the current project"
    override val usage: String = "hasab run [--release] [--args <arguments>]"

    override fun execute(args: List<String>): Int {
        val release = args.contains("--release")
        val programArgs = extractProgramArgs(args)
        val optProfile = if (release) OptProfile.Release else OptProfile.Debug

        println("Building and running project...")
        println()

        val config = ProjectConfig.load()
        val projectDir = File(".")
        val sourceDir = File(config.sourceDir)
        val outputDir = File(config.outputDir)

        if (!sourceDir.exists()) {
            Terminal.printError("Source directory '${config.sourceDir}' not found.")
            println("Run 'hasab new' to create a new project.")
            return 1
        }

        val pipelineConfig = PipelineConfig(
            backendType = BackendType.JAVA_SOURCE,
            optProfile = optProfile,
        )
        val pipeline = CompilerPipeline(config = pipelineConfig)

        Terminal.printBanner("Building: ${config.projectName} v${config.projectVersion}")
        if (release) Terminal.printInfo("Mode: Release (optimized)")

        val result = pipeline.compileProjectFromDirectory(projectDir, sourceDir, outputDir)

        if (result.compileErrors.isNotEmpty()) {
            printCompileErrors(result)
            return 1
        }

        if (result.javaErrors.isNotEmpty()) {
            printJavaErrors(result)
            return 1
        }

        println()
        Terminal.printSuccess("Build complete: ${result.modules.size} file(s) compiled")
        println()
        println("Running project...")
        println("=".repeat(50))
        println()

        return executeClass(result, config, programArgs)
    }

    private fun executeClass(
        result: PipelineResult,
        config: ProjectConfig,
        programArgs: List<String>,
    ): Int {
        val classesDir = File(config.outputDir, "classes")

        if (!classesDir.exists()) {
            System.err.println("Error: Build output not found at ${classesDir.path}")
            return 1
        }

        val mainClass = result.mainClassName.ifEmpty {
            config.entryPoint
        }

        println("Executing: $mainClass")
        if (programArgs.isNotEmpty()) {
            println("Arguments: ${programArgs.joinToString(" ")}")
        }
        println()

        return try {
            val processArgs = mutableListOf("java", "-cp", classesDir.absolutePath, mainClass)
            processArgs.addAll(programArgs)

            val processBuilder = ProcessBuilder(processArgs)
            processBuilder.inheritIO()
            val process = processBuilder.start()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                println()
                printRuntimeErrorWithSourceMap(result, classesDir)
            }

            exitCode
        } catch (e: java.io.IOException) {
            System.err.println("Error: Could not execute Java. Ensure JDK is installed and on PATH.")
            System.err.println("  ${e.message}")
            1
        } catch (e: Exception) {
            System.err.println("Error running project: ${e.message}")
            1
        }
    }

    private fun printRuntimeErrorWithSourceMap(result: PipelineResult, classesDir: File) {
        // Attempt to read any hs_err* files written by the JVM
        val errFile = File(".").listFiles()?.find { it.name.startsWith("hs_err") }
        if (errFile != null) {
            val trace = errFile.readText()
            println("JVM crash log: ${errFile.name}")
            println()

            val lineRegex = Regex("""at\s+(\w+)\.(\w+)\(([^:]*):(\d+)\)""")
            for (match in lineRegex.findAll(trace)) {
                val className = match.groupValues[1]
                val methodName = match.groupValues[2]
                val javaFile = match.groupValues[3]
                val javaLine = match.groupValues[4].toIntOrNull() ?: 0

                val sourceLoc = result.sourceMap.translateRuntimeTrace(className, javaLine)
                if (sourceLoc != null) {
                    println("  at $methodName(${sourceLoc.file}:${sourceLoc.line})")
                } else {
                    println("  at $className.$methodName($javaFile:$javaLine)")
                }
            }
            errFile.delete()
        }
    }

    private fun printCompileErrors(result: PipelineResult) {
        for (err in result.compileErrors) {
            val location = if (err.sourceLine > 0) "${err.sourceFile}:${err.sourceLine}:${err.sourceColumn}" else err.sourceFile
            if (err.severity == "error") {
                Terminal.printError("error[$location]: ${err.message}")
            } else {
                println("warning[$location]: ${err.message}")
            }
        }
        println()
        val errorCount = result.compileErrors.count { it.severity == "error" }
        Terminal.printError("Build failed: $errorCount error(s)")
    }

    private fun printJavaErrors(result: PipelineResult) {
        for (err in result.javaErrors) {
            val location = if (err.sourceLine > 0) {
                "${err.sourceFile}:${err.sourceLine}:${err.sourceColumn}"
            } else if (err.generatedLine > 0) {
                "${err.generatedFile}:${err.generatedLine}"
            } else {
                err.generatedFile
            }
            if (err.severity == "error") {
                Terminal.printError("javac error[$location]: ${err.message}")
            } else {
                println("javac warning[$location]: ${err.message}")
            }
        }
        println()
        val errorCount = result.javaErrors.count { it.severity == "error" }
        Terminal.printError("javac failed: $errorCount error(s)")
    }

    private fun extractProgramArgs(args: List<String>): List<String> {
        val idx = args.indexOf("--args")
        if (idx == -1 || idx + 1 >= args.size) return emptyList()
        return args.subList(idx + 1, args.size)
    }
}
