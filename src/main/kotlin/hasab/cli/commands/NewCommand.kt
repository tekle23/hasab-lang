package hasab.cli.commands

import hasab.cli.Command
import hasab.cli.config.ProjectConfig
import java.io.File

/**
 * Creates a new HASAB project with the standard directory structure.
 */
public class NewCommand : Command {
    override val name: String = "new"
    override val description: String = "Create a new HASAB project"
    override val usage: String = "hasab new <project-name> [--template <template>]"

    private val templates = mapOf(
        "default" to ::defaultTemplate,
        "web" to ::webTemplate,
        "api" to ::apiTemplate,
        "library" to ::libraryTemplate,
        "cli" to ::cliTemplate,
        "desktop" to ::desktopTemplate,
    )

    override fun execute(args: List<String>): Int {
        if (args.isEmpty()) {
            System.err.println("Error: Project name is required.")
            System.err.println("Usage: $usage")
            return 1
        }

        val projectName = args[0]
        val template = parseTemplate(args)
        val projectDir = File(projectName)

        if (projectDir.exists()) {
            System.err.println("Error: Directory '$projectName' already exists.")
            return 1
        }

        println("Creating new HASAB project: $projectName (template: $template)")

        projectDir.mkdirs()
        createDirectories(projectDir)
        createHasabToml(projectDir, projectName, template)
        createMainFile(projectDir, template)
        createTestFile(projectDir, projectName)
        createGitignore(projectDir)
        createReadme(projectDir, projectName)

        println("Project '$projectName' created successfully!")
        println()
        println("Next steps:")
        println("  cd $projectName")
        println("  hasab build")
        println("  hasab run")
        return 0
    }

    private fun parseTemplate(args: List<String>): String {
        val idx = args.indexOf("--template")
        return if (idx != -1 && idx + 1 < args.size) args[idx + 1] else "default"
    }

    private fun createDirectories(projectDir: File) {
        File(projectDir, "src").mkdirs()
        File(projectDir, "tests").mkdirs()
    }

    private fun createHasabToml(projectDir: File, projectName: String, template: String) {
        val config = ProjectConfig(
            projectName = projectName,
            projectVersion = "0.1.0",
            description = "A HASAB $template project",
            entryPoint = "main",
        )
        ProjectConfig.save(config, projectDir)
    }

    private fun createMainFile(projectDir: File, template: String) {
        val mainContent = templates[template]?.invoke(projectName(projectDir)) ?: defaultTemplate(projectName(projectDir))
        File(projectDir, "src/main.has").writeText(mainContent, Charsets.UTF_8)
    }

    private fun createTestFile(projectDir: File, projectName: String) {
        val content = """
            |fn test_basic_math() {
            |    println("tests running...");
            |
            |    if (1 + 1 == 2) {
            |        println("Basic math works!");
            |    } else {
            |        println("Math is broken!");
            |    }
            |
            |    println("All tests passed!");
            |}
        """.trimMargin()
        File(projectDir, "tests/main_test.has").writeText(content, Charsets.UTF_8)
    }

    private fun createGitignore(projectDir: File) {
        val content = """
            |# HASAB build artifacts
            |build/
            |*.class
            |*.jar
            |
            |# IDE
            |.idea/
            |.vscode/
            |*.iml
            |
            |# OS
            |.DS_Store
            |Thumbs.db
        """.trimMargin()
        File(projectDir, ".gitignore").writeText(content, Charsets.UTF_8)
    }

    private fun createReadme(projectDir: File, projectName: String) {
        val content = """
            |# $projectName
            |
            |A HASAB project created with `hasab new`.
            |
            |## Getting Started
            |
            |```bash
            |hasab build
            |hasab run
            |hasab test
            |```
            |
            |## Project Structure
            |
            |```
            |src/         - Source files
            |tests/       - Test files
            |hasab.toml   - Project configuration
            |```
        """.trimMargin()
        File(projectDir, "README.md").writeText(content, Charsets.UTF_8)
    }

    private fun projectName(projectDir: File): String = projectDir.name

    private fun defaultTemplate(name: String): String = """
        |// $name - HASAB Application
        |
        |fn main() {
        |    println("Hello from $name!");
        |    println("Welcome to HASAB!");
        |}
    """.trimMargin()

    private fun webTemplate(name: String): String = """
        |// $name - HASAB Web Application
        |
        |fn main() {
        |    println("Starting web server...");
        |    println("Server running on http://localhost:8080");
        |}
    """.trimMargin()

    private fun apiTemplate(name: String): String = """
        |// $name - HASAB API Service
        |
        |fn main() {
        |    println("Initializing API service...");
        |    println("API ready on port 3000");
        |}
    """.trimMargin()

    private fun libraryTemplate(name: String): String = """
        |// $name - HASAB Library
        |
        |/// Adds two numbers together
        |fn add(a: int, b: int): int {
        |    return a + b;
        |}
        |
        |fn main() {
        |    println("Library loaded: $name");
        |}
    """.trimMargin()

    private fun cliTemplate(name: String): String = """
        |// $name - HASAB CLI Application
        |
        |fn main() {
        |    println("$name v0.1.0");
        |    println("Use --help for usage information");
        |}
    """.trimMargin()

    private fun desktopTemplate(name: String): String = """
        |// $name - HASAB Desktop Application
        |
        |fn main() {
        |    println("Launching $name...");
        |    println("Desktop application initialized");
        |}
    """.trimMargin()
}
