package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.Terminal
import hasab.cli.config.ProjectConfig
import hasab.compiler.CompilerPipeline
import hasab.compiler.PipelineConfig
import hasab.compiler.backend.BackendType
import hasab.compiler.optimizer.OptProfile
import java.io.File

/**
 * Compiles all .has source files in the project.
 *
 * Pipeline: source → lex → parse → semantic → typecheck → HIR → optimize → Java → javac
 */
public class BuildCommand : Command {
    override val name: String = "build"
    override val description: String = "Compile the current project"
    override val usage: String = "hasab build [--release]"

    override fun execute(args: List<String>): Int {
        val release = args.contains("--release")
        val optProfile = if (release) OptProfile.Release else OptProfile.Debug

        val config = ProjectConfig.load()
        val projectDir = File(".")
        val sourceDir = File(config.sourceDir)
        val outputDir = File(config.outputDir)

        Terminal.printBanner("Building: ${config.projectName} v${config.projectVersion}")
        if (release) Terminal.printInfo("Mode: Release (optimized)")

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
        val result = pipeline.compileProjectFromDirectory(projectDir, sourceDir, outputDir)

        if (result.compileErrors.isNotEmpty()) {
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
            return 1
        }

        if (result.javaErrors.isNotEmpty()) {
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
            return 1
        }

        println()
        Terminal.printSuccess("Build complete: ${result.modules.size} file(s) compiled")
        Terminal.printInfo("Output: ${config.outputJarPath()}")
        return 0
    }
}
