package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.HasabToml
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Removes a dependency from the project's hasab.toml.
 */
public class RemoveCommand : Command {
    override val name: String = "remove"
    override val description: String = "Remove a dependency"
    override val usage: String = "hasab remove <package>"

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            System.err.println("Error: Package name is required.")
            System.err.println("Usage: $usage")
            return 1
        }

        val packageName = args[0].lowercase()
        val tomlFile = File("hasab.toml")
        if (!tomlFile.exists()) {
            System.err.println("Error: No hasab.toml found. Run 'hasab new' to create a project.")
            return 1
        }

        val data = HasabToml.parse(tomlFile).toMutableMap()
        val depKey = "dependencies.$packageName"

        if (!data.containsKey(depKey)) {
            System.err.println("Error: Dependency '$packageName' not found in hasab.toml.")
            return 1
        }

        data.remove(depKey)
        val config = ProjectConfig.fromToml(data)
        ProjectConfig.save(config, File("."))

        println("Removed dependency '$packageName' from hasab.toml")
        return 0
    }
}
