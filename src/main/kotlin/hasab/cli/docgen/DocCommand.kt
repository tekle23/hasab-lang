package hasab.cli.docgen

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Generates documentation from source code doc comments.
 */
public class DocCommand : Command {
    override val name: String = "doc"
    override val description: String = "Generate documentation from source"
    override val usage: String = "hasab doc [-o output.md]"

    override fun execute(args: List<String>): Int {
        val config = ProjectConfig.load()
        val srcDir = File(config.sourceDir)
        val outputIdx = args.indexOf("-o")
        val outputFile = if (outputIdx >= 0 && outputIdx + 1 < args.size) {
            File(args[outputIdx + 1])
        } else {
            File(config.outputDir, "docs.md")
        }

        if (!srcDir.exists()) {
            println("Source directory not found: ${srcDir.path}")
            return 1
        }

        val generator = HasabDocGenerator()
        generator.generateForDirectory(srcDir, outputFile)

        println("Documentation generated: ${outputFile.path}")
        return 0
    }
}
