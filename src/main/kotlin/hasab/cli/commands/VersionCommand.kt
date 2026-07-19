package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.HasabCli
import hasab.runtime.services.HsVersion

/**
 * Displays version information about the HASAB toolchain.
 */
public class VersionCommand : Command {
    override val name: String = "version"
    override val description: String = "Show version information"

    override fun execute(args: List<String>): Int {
        println("HASAB Toolchain v${HasabCli.VERSION}")
        println("HASAB Runtime v${HsVersion.version}")
        println("Kotlin:     ${HsVersion.kotlinVersion}")
        println("Java:       ${HsVersion.javaVersion}")
        println("Build Date: ${HsVersion.buildDate}")
        return 0
    }
}
