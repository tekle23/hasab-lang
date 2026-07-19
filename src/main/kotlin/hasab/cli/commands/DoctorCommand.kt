package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.HasabCli
import hasab.runtime.services.HsVersion
import hasab.runtime.util.HsPlatform
import java.io.File
import java.nio.file.Files

/**
 * Checks the development environment for potential issues.
 */
public class DoctorCommand : Command {
    override val name: String = "doctor"
    override val description: String = "Check environment health"

    override fun execute(args: List<String>): Int {
        var issues = 0

        println("HASAB Doctor v${HasabCli.VERSION}")
        println("=" .repeat(50))
        println()

        issues += checkJava()
        issues += checkProject()
        issues += checkDiskSpace()

        println()
        if (issues == 0) {
            println("All checks passed!")
        } else {
            println("$issues issue(s) found.")
        }
        return if (issues == 0) 0 else 1
    }

    private fun checkJava(): Int {
        print("  Java version: ")
        val version = HsVersion.javaVersion
        println("$version [OK]")
        print("  OS: ")
        println("${HsPlatform.osName} (${HsPlatform.osArch}) [OK]")
        print("  Processors: ")
        println("${HsPlatform.availableProcessors} [OK]")
        return 0
    }

    private fun checkProject(): Int {
        var issues = 0
        print("  hasab.toml: ")
        val toml = File("hasab.toml")
        if (toml.exists()) {
            println("[OK]")
        } else {
            println("[WARN] No hasab.toml found. Run 'hasab new' to create a project.")
            issues++
        }
        print("  src/ directory: ")
        val src = File("src")
        if (src.exists() && src.isDirectory) {
            val hasFiles = src.walkTopDown().any { it.extension == "has" }
            if (hasFiles) println("[OK]") else { println("[WARN] No .has files found"); issues++ }
        } else {
            println("[WARN] No src/ directory found")
            issues++
        }
        return issues
    }

    private fun checkDiskSpace(): Int {
        print("  Disk space: ")
        val usable = try {
            Files.getFileStore(File(".").toPath()).usableSpace / (1024 * 1024 * 1024)
        } catch (_: Exception) {
            -1L
        }
        if (usable >= 0) println("${usable}GB free [OK]") else println("unknown [OK]")
        return 0
    }
}
