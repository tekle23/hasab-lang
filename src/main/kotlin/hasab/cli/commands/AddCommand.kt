package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.HasabToml
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Adds a dependency to the project's hasab.toml.
 */
public class AddCommand : Command {
    override val name: String = "add"
    override val description: String = "Add a dependency"
    override val usage: String = "hasab add <package> [--version <version>]"

    private val registry = PackageRegistry()

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            System.err.println("Error: Package name is required.")
            System.err.println("Usage: $usage")
            return 1
        }

        val packageName = args[0]
        val version = args.indexOf("--version").let { idx ->
            if (idx != -1 && idx + 1 < args.size) args[idx + 1] else "latest"
        }

        val tomlFile = File("hasab.toml")
        if (!tomlFile.exists()) {
            System.err.println("Error: No hasab.toml found. Run 'hasab new' to create a project.")
            return 1
        }

        val data = HasabToml.parse(tomlFile).toMutableMap()
        val depKey = "dependencies.$packageName"
        data[depKey.lowercase()] = version

        println("Adding dependency: $packageName@$version")

        val resolvedVersion = if (version == "latest") {
            registry.resolveLatest(packageName)
        } else {
            version
        }

        if (resolvedVersion == null) {
            System.err.println("Warning: Could not verify package '$packageName' in registry.")
            System.err.println("Adding anyway with version '$version'.")
            data[depKey.lowercase()] = version
        } else {
            data[depKey.lowercase()] = resolvedVersion
            println("Resolved version: $resolvedVersion")
        }

        val config = ProjectConfig.fromToml(data)
        ProjectConfig.save(config, File("."))

        println("Dependency '$packageName' added to hasab.toml")
        return 0
    }
}
