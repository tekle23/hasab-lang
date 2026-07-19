package hasab.cli

/**
 * Routes CLI arguments to the appropriate [Command].
 */
public class CommandRouter(private val commands: Map<String, Command>) {

    /**
     * Dispatches the argument list to the matching command.
     *
     * @return The exit code returned by the command.
     */
    public fun dispatch(args: List<String>): Int {
        if (args.isEmpty()) {
            printHelp()
            return 0
        }
        val commandName = args[0]
        val command = commands[commandName]
        if (command == null) {
            System.err.println("Unknown command: $commandName")
            System.err.println("Run 'hasab help' for a list of available commands.")
            return 1
        }
        return command.execute(args.drop(1))
    }

    private fun printHelp() {
        println("HASAB Programming Language Toolchain v${HasabCli.VERSION}")
        println()
        println("Usage: hasab <command> [options]")
        println()
        println("Available commands:")
        if (commands.isNotEmpty()) {
            val maxLen = commands.keys.maxOf { it.length }
            for ((name, cmd) in commands.toSortedMap()) {
                println("  ${name.padEnd(maxLen + 2)}${cmd.description}")
            }
        }
        println()
        println("Run 'hasab help <command>' for more information on a specific command.")
    }
}
