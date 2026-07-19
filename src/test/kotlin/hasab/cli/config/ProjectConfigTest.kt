package hasab.cli.config

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class ProjectConfigTest {

    @Test
    public fun `default values are correct`() {
        val config = ProjectConfig()
        assertEquals("untitled", config.projectName)
        assertEquals("0.1.0", config.projectVersion)
        assertEquals("", config.description)
        assertEquals("", config.author)
        assertEquals("src", config.sourceDir)
        assertEquals("tests", config.testDir)
        assertEquals("main", config.entryPoint)
        assertEquals("build", config.outputDir)
        assertTrue(config.dependencies.isEmpty())
        assertEquals("https://packages.hasab.org", config.repository)
        assertEquals("21", config.kotlinJvmTarget)
    }

    @Test
    public fun `entryPointPath computes correctly`() {
        val config = ProjectConfig(projectName = "myapp", sourceDir = "src", entryPoint = "main")
        assertEquals("src/main.has", config.entryPointPath())
    }

    @Test
    public fun `outputJarPath computes correctly`() {
        val config = ProjectConfig(projectName = "myapp", outputDir = "build")
        assertEquals("build/myapp.jar", config.outputJarPath())
    }

    @Test
    public fun `classesDir computes correctly`() {
        val config = ProjectConfig(outputDir = "build")
        assertEquals("build/classes", config.classesDir())
    }

    @Test
    public fun `tmpDir computes correctly`() {
        val config = ProjectConfig(outputDir = "build")
        assertEquals("build/tmp", config.tmpDir())
    }

    @Test
    public fun `fromToml parses package fields`() {
        val data = mapOf(
            "package.name" to "parsed",
            "package.version" to "3.0",
            "package.description" to "A test project",
            "package.author" to "Tester",
        )
        val config = ProjectConfig.fromToml(data)
        assertEquals("parsed", config.projectName)
        assertEquals("3.0", config.projectVersion)
        assertEquals("A test project", config.description)
        assertEquals("Tester", config.author)
    }

    @Test
    public fun `fromToml parses project fields`() {
        val data = mapOf(
            "project.source" to "source",
            "project.tests" to "test",
            "project.entry" to "app",
            "project.output" to "dist",
            "project.jvm_target" to "17",
        )
        val config = ProjectConfig.fromToml(data)
        assertEquals("source", config.sourceDir)
        assertEquals("test", config.testDir)
        assertEquals("app", config.entryPoint)
        assertEquals("dist", config.outputDir)
        assertEquals("17", config.kotlinJvmTarget)
    }

    @Test
    public fun `fromToml parses dependencies`() {
        val data = mapOf(
            "dependencies.web" to "1.0",
            "dependencies.json" to "2.1",
        )
        val config = ProjectConfig.fromToml(data)
        assertEquals(2, config.dependencies.size)
        assertEquals("1.0", config.dependencies["web"])
        assertEquals("2.1", config.dependencies["json"])
    }

    @Test
    public fun `fromToml uses defaults for missing fields`() {
        val config = ProjectConfig.fromToml(emptyMap())
        assertEquals("untitled", config.projectName)
        assertEquals("0.1.0", config.projectVersion)
    }

    @Test
    public fun `save and load roundtrip`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_test_${System.nanoTime()}")
        dir.mkdirs()
        try {
            val config = ProjectConfig(
                projectName = "roundtrip",
                projectVersion = "1.2.3",
                description = "Test project",
                author = "Tester",
                dependencies = mapOf("web" to "1.0"),
            )
            ProjectConfig.save(config, dir)

            val loaded = ProjectConfig.load(dir)
            assertEquals("roundtrip", loaded.projectName)
            assertEquals("1.2.3", loaded.projectVersion)
            assertEquals("Test project", loaded.description)
            assertEquals("Tester", loaded.author)
            assertEquals("1.0", loaded.dependencies["web"])
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    public fun `load returns defaults when no toml file exists`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "hasab_empty_${System.nanoTime()}")
        dir.mkdirs()
        try {
            val config = ProjectConfig.load(dir)
            assertEquals("untitled", config.projectName)
        } finally {
            dir.deleteRecursively()
        }
    }
}
