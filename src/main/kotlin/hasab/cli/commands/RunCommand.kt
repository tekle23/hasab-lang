package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Builds and executes the current project.
 */
public class RunCommand : Command {
    override val name: String = "run"
    override val description: String = "Build and run the current project"
    override val usage: String = "hasab run [--args <arguments>]"

    private val buildCommand = BuildCommand()

    override fun execute(args: List<String>): Int {
        println("Building project...")
        println()

        val buildArgs = mutableListOf<String>()
        if (args.contains("--release")) buildArgs.add("--release")

        val buildResult = buildCommand.execute(buildArgs)
        if (buildResult != 0) {
            System.err.println("\nBuild failed. Cannot run.")
            return 1
        }

        println()
        println("Running project...")
        println("=".repeat(50))
        println()

        val config = ProjectConfig.load()
        val javaDir = File(config.classesDir())

        if (!javaDir.exists()) {
            System.err.println("Error: Build output not found at ${javaDir.path}")
            return 1
        }

        val javaFiles = javaDir.walkTopDown().filter { it.extension == "java" }.toList()
        if (javaFiles.isEmpty()) {
            System.err.println("Error: No compiled Java files found.")
            return 1
        }

        val mainFile = javaFiles.find { it.nameWithoutExtension == config.entryPoint }
            ?: javaFiles.first()

        println("Executing: ${mainFile.nameWithoutExtension}")
        println()

        return try {
            val processBuilder = ProcessBuilder(
                "java", "-cp", javaDir.absolutePath,
                mainFile.nameWithoutExtension
            )
            processBuilder.inheritIO()
            val process = processBuilder.start()
            process.waitFor()
        } catch (e: Exception) {
            System.err.println("Error running project: ${e.message}")
            1
        }
    }
}
