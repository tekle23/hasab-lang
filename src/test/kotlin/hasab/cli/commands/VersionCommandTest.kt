package hasab.cli.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class VersionCommandTest {

    @Test
    public fun `version command has correct name`() {
        assertEquals("version", VersionCommand().name)
    }

    @Test
    public fun `version command returns 0`() {
        val result = VersionCommand().execute(emptyList())
        assertEquals(0, result)
    }

    @Test
    public fun `version command description is set`() {
        assertTrue(VersionCommand().description.isNotEmpty())
    }
}
