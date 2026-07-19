package hasab.cli

/**
 * Terminal output utilities for colored, rich CLI diagnostics.
 *
 * Supports ANSI escape codes for Windows, Linux, and macOS.
 * Falls back to plain text when stdout is not a TTY.
 */
public object Terminal {

    public const val RESET: String = "\u001B[0m"
    public const val RED: String = "\u001B[31m"
    public const val GREEN: String = "\u001B[32m"
    public const val YELLOW: String = "\u001B[33m"
    public const val BLUE: String = "\u001B[34m"
    public const val MAGENTA: String = "\u001B[35m"
    public const val CYAN: String = "\u001B[36m"
    public const val BOLD: String = "\u001B[1m"
    public const val DIM: String = "\u001B[2m"

    public val enabled: Boolean by lazy {
        val term = System.getenv("TERM")
        val ci = System.getenv("CI")
        val noColor = System.getenv("NO_COLOR")
        noColor == null && (ci != null || term != null || System.console() != null)
    }

    public fun colorize(color: String, text: String): String {
        if (!enabled) return text
        return "$color$text$RESET"
    }

    public fun success(text: String): String = colorize(GREEN, text)
    public fun error(text: String): String = colorize(RED, text)
    public fun warn(text: String): String = colorize(YELLOW, text)
    public fun info(text: String): String = colorize(CYAN, text)
    public fun dim(text: String): String = colorize(DIM, text)
    public fun bold(text: String): String = colorize(BOLD, text)

    public fun printSuccess(message: String) {
        println("  ${success("ok")} $message")
    }

    public fun printError(message: String) {
        System.err.println("  ${error("error")} $message")
    }

    public fun printWarning(message: String) {
        println("  ${warn("warn")} $message")
    }

    public fun printInfo(message: String) {
        println("  ${info("info")} $message")
    }

    public fun printStep(current: Int, total: Int, message: String) {
        val label = "[$current/$total]"
        println("  ${dim(label)} ${bold(message)}")
    }

    public fun printBanner(title: String) {
        val width = 50
        println()
        println(colorize(BOLD, title))
        println(colorize(DIM, "=".repeat(width)))
        println()
    }

    public fun printTable(rows: List<Pair<String, String>>, indent: String = "  ") {
        val maxKey = rows.maxOfOrNull { it.first.length } ?: 0
        for ((key, value) in rows) {
            println("$indent${key.padEnd(maxKey + 2)}$value")
        }
    }

    public fun withProgressBar(total: Int, block: (progress: (Int) -> Unit) -> Unit) {
        var current = 0
        fun progress() {
            current++
            val pct = (current * 100) / total
            val barLen = 30
            val filled = (pct * barLen) / 100
            val bar = "█".repeat(filled) + "░".repeat(barLen - filled)
            System.out.print("\r  $bar $pct% ($current/$total)")
            System.out.flush()
        }
        block { progress() }
        println()
    }
}
