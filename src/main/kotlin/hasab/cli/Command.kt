package hasab.cli

/**
 * Interface that all CLI commands must implement.
 */
public interface Command {
    /** The name of the command as typed by the user. */
    public val name: String

    /** A short one-line description shown in help output. */
    public val description: String

    /** Usage string showing arguments/options. */
    public val usage: String get() = "hasab $name"

    /** Execute the command with the given arguments. */
    public fun execute(args: List<String>): Int
}
