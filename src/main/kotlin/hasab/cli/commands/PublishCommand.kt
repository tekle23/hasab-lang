package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Publishes the current package to the registry.
 *
 * In this v1.0 implementation, publish performs a dry-run validation
 * and creates a build artifact. Actual registry upload is simulated.
 */
public class PublishCommand : Command {
    override val name: String = "publish"
    override val description: String = "Publish the package to the registry"
    override val usage: String = "hasab publish [--dry-run]"

    override fun execute(args: List<String>): Int {
        val dryRun = args.contains("--dry-run")
        val config = ProjectConfig.load()

        if (config.projectName == "untitled") {
            System.err.println("Error: No project name configured. Update hasab.toml.")
            return 1
        }

        println("Package: ${config.projectName}@${config.projectVersion}")
        println("Author: ${config.author.ifEmpty { "(not set)" }}")
        println("Repository: ${config.repository}")
        println()

        println("Validating package...")
        val issues = validateProject(config)
        if (issues.isNotEmpty()) {
            for (issue in issues) {
                System.err.println("  [ERROR] $issue")
            }
            return 1
        }
        println("  Validation passed.")

        println("Building package...")
        val buildResult = BuildCommand().execute(emptyList())
        if (buildResult != 0) {
            System.err.println("  Build failed. Cannot publish.")
            return 1
        }
        println("  Build succeeded.")

        if (dryRun) {
            println()
            println("Dry run complete. No package was published.")
            return 0
        }

        println()
        println("Publishing ${config.projectName}@${config.projectVersion}...")
        println("  Package built: ${config.outputJarPath()}")
        println("  Simulated upload to ${config.repository}")
        println("  Package published successfully!")
        return 0
    }

    private fun validateProject(config: ProjectConfig): List<String> {
        val issues = mutableListOf<String>()

        if (config.projectName.isBlank()) {
            issues.add("Project name is required in hasab.toml")
        }
        if (config.projectVersion.isBlank()) {
            issues.add("Project version is required in hasab.toml")
        }

        val srcDir = File(config.sourceDir)
        if (!srcDir.exists() || !srcDir.isDirectory) {
            issues.add("Source directory '${config.sourceDir}' does not exist")
        } else {
            val hasFiles = srcDir.walkTopDown().any { it.extension == "has" }
            if (!hasFiles) {
                issues.add("No .has files found in '${config.sourceDir}'")
            }
        }

        return issues
    }
}
