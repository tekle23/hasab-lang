package hasab.cli.commands

import kotlin.test.Test
import kotlin.test.assertTrue

public class CleanCommandTest {

    @Test
    public fun `clean command has correct name`() {
        assertTrue(CleanCommand().name == "clean")
    }

    @Test
    public fun `clean command returns 0`() {
        val result = CleanCommand().execute(emptyList())
        assertTrue(result == 0)
    }

    @Test
    public fun `clean command description is set`() {
        assertTrue(CleanCommand().description.isNotEmpty())
    }
}
