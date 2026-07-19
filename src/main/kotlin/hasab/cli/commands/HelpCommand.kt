package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.HasabCli

/**
 * Displays help information for the HASAB CLI or a specific command.
 */
public class HelpCommand : Command {
    override val name: String = "help"
    override val description: String = "Display help information"

    override fun execute(args: List<String>): Int {
        if (args.isNotEmpty()) {
            return showCommandHelp(args[0])
        }
        showGeneralHelp()
        return 0
    }

    private fun showGeneralHelp() {
        println("""
            |HASAB Programming Language Toolchain v${HasabCli.VERSION}
            |
            |Usage: hasab <command> [options]
            |
            |Project Commands:
            |  new <name>       Create a new HASAB project
            |  build            Compile the current project
            |  run              Build and run the current project
            |  test             Run project tests
            |  clean            Remove build artifacts
            |
            |Development Commands:
            |  fmt              Format source files
            |  lint             Lint source files for issues
            |  doc              Generate documentation
            |
            |Package Commands:
            |  add <package>    Add a dependency
            |  remove <package> Remove a dependency
            |  publish          Publish to registry
            |
            |Utility Commands:
            |  doctor           Check environment health
            |  version          Show version information
            |  help             Show this help message
            |
            |Run 'hasab help <command>' for detailed help on a specific command.
        """.trimMargin())
    }

    private fun showCommandHelp(commandName: String): Int {
        val descriptions = mapOf(
            "new" to """
                |Usage: hasab new <project-name> [--template <template>]
                |
                |Creates a new HASAB project with the standard directory structure.
                |
                |Templates: default, web, api, library, cli, desktop
                |
                |Example:
                |  hasab new HelloWorld
                |  hasab new MyWebApp --template web
            """,
            "build" to """
                |Usage: hasab build [--release]
                |
                |Compiles all .has source files in the current project.
                |Produces a .jar file in the build/ directory.
                |
                |Options:
                |  --release    Build with optimizations enabled
            """,
            "run" to """
                |Usage: hasab run [--args <arguments>]
                |
                |Builds and executes the current project.
                |
                |Options:
                |  --args       Arguments to pass to the program
            """,
            "test" to """
                |Usage: hasab test [--filter <pattern>]
                |
                |Runs all tests in the tests/ directory.
                |
                |Options:
                |  --filter     Only run tests matching the pattern
            """,
            "fmt" to """
                |Usage: hasab fmt [--check] [--diff]
                |
                |Formats all .has source files in the project.
                |
                |Options:
                |  --check      Check if formatting is needed (exit 1 if so)
                |  --diff       Show the formatting diff without applying
            """,
            "lint" to """
                |Usage: hasab lint [--fix]
                |
                |Lints all .has source files for common issues.
                |
                |Options:
                |  --fix        Automatically fix safe issues
            """,
            "doc" to """
                |Usage: hasab doc [--format <format>] [--output <dir>]
                |
                |Generates documentation from /// doc comments.
                |
                |Options:
                |  --format     Output format: html, markdown, json (default: html)
                |  --output     Output directory (default: docs/)
            """,
            "add" to """
                |Usage: hasab add <package> [--version <version>]
                |
                |Adds a dependency to the project's hasab.toml.
                |
                |Example:
                |  hasab add web
                |  hasab add json --version 2.1
            """,
            "remove" to """
                |Usage: hasab remove <package>
                |
                |Removes a dependency from the project's hasab.toml.
                |
                |Example:
                |  hasab remove web
            """,
            "publish" to """
                |Usage: hasab publish [--dry-run]
                |
                |Publishes the current package to the registry.
                |Performs validation and build before publishing.
                |
                |Options:
                |  --dry-run     Validate and build without uploading
            """,
            "clean" to """
                |Usage: hasab clean
                |
                |Removes the build/ directory and all compiled artifacts.
            """,
            "doctor" to """
                |Usage: hasab doctor
                |
                |Checks the development environment for potential issues.
                |Verifies Java, Kotlin, and project configuration.
            """,
            "version" to """
                |Usage: hasab version
                |
                |Displays version information for the HASAB toolchain.
            """,
        )

        val help = descriptions[commandName]
        if (help != null) {
            println(help.trimMargin())
            return 0
        }
        System.err.println("Unknown command: $commandName")
        return 1
    }
}
