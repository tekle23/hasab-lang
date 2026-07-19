package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.Terminal
import hasab.cli.config.ProjectConfig
import hasab.compiler.backend.HasabToJavaCompiler
import java.io.File

/**
 * Compiles all .has source files in the project.
 */
public class BuildCommand : Command {
    override val name: String = "build"
    override val description: String = "Compile the current project"
    override val usage: String = "hasab build [--release]"

    override fun execute(args: List<String>): Int {
        val release = args.contains("--release")
        val config = ProjectConfig.load()

        Terminal.printBanner("Building: ${config.projectName} v${config.projectVersion}")
        if (release) Terminal.printInfo("Mode: Release (optimized)")

        val srcDir = File(config.sourceDir)
        if (!srcDir.exists()) {
            Terminal.printError("Source directory '${config.sourceDir}' not found.")
            println("Run 'hasab new' to create a new project.")
            return 1
        }

        val hasFiles = srcDir.walkTopDown().filter { it.extension == "has" }.toList()
        if (hasFiles.isEmpty()) {
            System.err.println("Error: No .has files found in '${config.sourceDir}'.")
            return 1
        }

        val buildDir = File(config.outputDir)
        val classesDir = File(config.classesDir())
        buildDir.mkdirs()
        classesDir.mkdirs()

        var successCount = 0
        var errorCount = 0

        for (file in hasFiles) {
            val relativePath = file.relativeTo(srcDir).path
            print("  Compiling $relativePath ... ")

            val sourceCode = file.readText(Charsets.UTF_8)
            val result = HasabToJavaCompiler.compile(sourceCode, relativePath)

            if (result.hasErrors) {
                println(Terminal.error("FAILED"))
                for (diag in result.typeDiagnostics) {
                    Terminal.printError("  ${diag.message}")
                }
                errorCount++
            } else {
                val javaFile = File(classesDir, relativePath.replace(".has", ".java"))
                javaFile.parentFile?.mkdirs()
                javaFile.writeText(result.javaSource, Charsets.UTF_8)
                println(Terminal.success("OK"))
                successCount++
            }
        }

        println()
        if (errorCount == 0) {
            Terminal.printSuccess("Build complete: $successCount file(s) compiled")
            Terminal.printInfo("Output: ${config.outputJarPath()}")
        } else {
            Terminal.printError("Build failed: $errorCount error(s), $successCount succeeded")
        }

        return if (errorCount == 0) 0 else 1
    }
}
