package hasab.cli.config

import java.io.File

/**
 * Represents the parsed project configuration from hasab.toml.
 *
 * @property projectName The project name.
 * @property projectVersion The project version.
 * @property description Project description.
 * @property author Project author.
 * @property sourceDir Source directory path.
 * @property testDir Test directory path.
 * @property entryPoint Main entry point file.
 * @property outputDir Build output directory.
 * @property dependencies Map of dependency names to versions.
 * @property repository Package repository URL.
 * @property kotlinJvmTarget JVM target version.
 */
public data class ProjectConfig(
    public val projectName: String = "untitled",
    public val projectVersion: String = "0.1.0",
    public val description: String = "",
    public val author: String = "",
    public val sourceDir: String = "src",
    public val testDir: String = "tests",
    public val entryPoint: String = "main",
    public val outputDir: String = "build",
    public val dependencies: Map<String, String> = emptyMap(),
    public val repository: String = "https://packages.hasab.org",
    public val kotlinJvmTarget: String = "21",
) {

    /** Returns the full path to the entry point .has file. */
    public fun entryPointPath(): String = "$sourceDir/$entryPoint.has"

    /** Returns the build output JAR path. */
    public fun outputJarPath(): String = "$outputDir/$projectName.jar"

    /** Returns the class directory. */
    public fun classesDir(): String = "$outputDir/classes"

    /** Returns the temporary build directory. */
    public fun tmpDir(): String = "$outputDir/tmp"

    public companion object {

        /**
         * Loads the project configuration from the current directory.
         */
        public fun load(): ProjectConfig = load(File("."))

        /**
         * Loads the project configuration from the given directory.
         */
        public fun load(directory: File): ProjectConfig {
            val tomlFile = File(directory, "hasab.toml")
            if (!tomlFile.exists()) {
                return ProjectConfig()
            }
            return fromToml(HasabToml.parse(tomlFile))
        }

        /**
         * Creates a [ProjectConfig] from parsed TOML data.
         */
        public fun fromToml(data: Map<String, String>): ProjectConfig {
            val deps = mutableMapOf<String, String>()
            for ((key, value) in data) {
                if (key.startsWith("dependencies.")) {
                    val depName = key.removePrefix("dependencies.")
                    deps[depName] = value
                }
            }

            return ProjectConfig(
                projectName = data["package.name"] ?: "untitled",
                projectVersion = data["package.version"] ?: "0.1.0",
                description = data["package.description"] ?: "",
                author = data["package.author"] ?: "",
                sourceDir = data["project.source"] ?: "src",
                testDir = data["project.tests"] ?: "tests",
                entryPoint = data["project.entry"] ?: "main",
                outputDir = data["project.output"] ?: "build",
                dependencies = deps,
                repository = data["package.repository"] ?: "https://packages.hasab.org",
                kotlinJvmTarget = data["project.jvm_target"] ?: "21",
            )
        }

        /**
         * Saves the project configuration to a hasab.toml file.
         */
        public fun save(config: ProjectConfig, directory: File) {
            val sb = StringBuilder()
            sb.appendLine("[package]")
            sb.appendLine("name = \"${config.projectName}\"")
            sb.appendLine("version = \"${config.projectVersion}\"")
            if (config.description.isNotEmpty()) sb.appendLine("description = \"${config.description}\"")
            if (config.author.isNotEmpty()) sb.appendLine("author = \"${config.author}\"")
            sb.appendLine("repository = \"${config.repository}\"")
            sb.appendLine()
            sb.appendLine("[project]")
            sb.appendLine("source = \"${config.sourceDir}\"")
            sb.appendLine("tests = \"${config.testDir}\"")
            sb.appendLine("entry = \"${config.entryPoint}\"")
            sb.appendLine("output = \"${config.outputDir}\"")
            sb.appendLine("jvm_target = \"${config.kotlinJvmTarget}\"")

            if (config.dependencies.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("[dependencies]")
                for ((name, version) in config.dependencies.toSortedMap()) {
                    sb.appendLine("$name = \"$version\"")
                }
            }

            File(directory, "hasab.toml").writeText(sb.toString(), Charsets.UTF_8)
        }
    }
}
