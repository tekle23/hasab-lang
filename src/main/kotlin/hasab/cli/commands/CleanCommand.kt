package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Removes build artifacts.
 */
public class CleanCommand : Command {
    override val name: String = "clean"
    override val description: String = "Remove build artifacts"

    override fun execute(args: List<String>): Int {
        val config = ProjectConfig.load()
        val buildDir = File(config.outputDir)

        if (!buildDir.exists()) {
            println("Nothing to clean.")
            return 0
        }

        print("Cleaning ${buildDir.path} ... ")
        val deleted = buildDir.deleteRecursively()
        if (deleted) {
            println("done")
        } else {
            System.err.println("warning: some files could not be deleted")
        }
        return 0
    }
}
