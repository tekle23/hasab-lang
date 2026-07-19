package hasab.cli.commands

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class PublishCommandTest {

    @Test
    public fun `publish command has correct name`() {
        assertEquals("publish", PublishCommand().name)
    }

    @Test
    public fun `publish fails when no project configured`() {
        val result = PublishCommand().execute(emptyList())
        assertTrue(result != 0)
    }

    @Test
    public fun `publish command description is set`() {
        assertTrue(PublishCommand().description.isNotEmpty())
    }

    @Test
    public fun `publish usage includes dry-run`() {
        assertTrue(PublishCommand().usage.contains("--dry-run"))
    }
}
