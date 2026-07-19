package hasab.cli

import hasab.cli.commands.*
import hasab.cli.fmt.FmtCommand
import hasab.cli.lint.LintCommand
import hasab.cli.docgen.DocCommand

/**
 * Main entry point for the HASAB CLI toolchain.
 *
 * Parses command-line arguments and routes to the appropriate subcommand.
 */
public object HasabCli {

    /** The current version of the HASAB CLI. */
    public const val VERSION: String = "1.0.0"

    private val commands: Map<String, Command> by lazy {
        buildMap {
            put("new", NewCommand())
            put("build", BuildCommand())
            put("run", RunCommand())
            put("test", TestCommand())
            put("fmt", FmtCommand())
            put("lint", LintCommand())
            put("doc", DocCommand())
            put("add", AddCommand())
            put("remove", RemoveCommand())
            put("publish", PublishCommand())
            put("clean", CleanCommand())
            put("doctor", DoctorCommand())
            put("version", VersionCommand())
            put("help", HelpCommand())
        }
    }

    private val router: CommandRouter = CommandRouter(commands)

    /**
     * Entry point. Returns an exit code.
     */
    public fun run(args: Array<String>): Int = router.dispatch(args.toList())

    /**
     * Main function for JVM execution.
     */
    @JvmStatic
    public fun main(args: Array<String>) {
        val exitCode = run(args)
        if (exitCode != 0) {
            kotlin.system.exitProcess(exitCode)
        }
    }
}
