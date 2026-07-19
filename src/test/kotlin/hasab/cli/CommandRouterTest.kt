package hasab.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class CommandRouterTest {

    private object StubCommand : Command {
        override val name: String = "stub"
        override val description: String = "A stub command"
        override val usage: String = "stub [args]"

        var lastArgs: List<String> = emptyList()
        var executeResult: Int = 0

        override fun execute(args: List<String>): Int {
            lastArgs = args
            return executeResult
        }
    }

    @Test
    public fun `dispatch routes to correct command`() {
        StubCommand.executeResult = 0
        val router = CommandRouter(mapOf("stub" to StubCommand))
        val result = router.dispatch(listOf("stub"))
        assertEquals(0, result)
        assertEquals(emptyList(), StubCommand.lastArgs)
    }

    @Test
    public fun `dispatch passes arguments to command`() {
        StubCommand.executeResult = 0
        val router = CommandRouter(mapOf("stub" to StubCommand))
        router.dispatch(listOf("stub", "foo", "bar"))
        assertEquals(listOf("foo", "bar"), StubCommand.lastArgs)
    }

    @Test
    public fun `dispatch returns non-zero for unknown command`() {
        val router = CommandRouter(emptyMap())
        val result = router.dispatch(listOf("unknown"))
        assertTrue(result != 0)
    }

    @Test
    public fun `dispatch returns 0 for empty args`() {
        val router = CommandRouter(emptyMap())
        val result = router.dispatch(emptyList())
        assertEquals(0, result)
    }

    @Test
    public fun `dispatch with help command`() {
        val helpCmd = object : Command {
            override val name: String = "help"
            override val description: String = "help"
            override val usage: String = "help"
            override fun execute(args: List<String>): Int = 0
        }
        val router = CommandRouter(mapOf("help" to helpCmd))
        val result = router.dispatch(listOf("help"))
        assertEquals(0, result)
    }
}
