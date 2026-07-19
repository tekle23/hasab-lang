package hasab.runtime.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsLogTest {

    @Test
    public fun `debug message is emitted when level is DEBUG`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.debug("hello")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("DEBUG"))
            assertTrue(messages[0].contains("hello"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `debug message is suppressed when level is INFO`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.INFO
            HsLog.debug("hidden")
            assertEquals(0, messages.size)
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `info message is emitted when level is INFO`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.INFO
            HsLog.info("hello info")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("INFO"))
            assertTrue(messages[0].contains("hello info"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `warn message is emitted when level is WARNING`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.WARNING
            HsLog.warn("warn msg")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("WARNING"))
            assertTrue(messages[0].contains("warn msg"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `error message is emitted when level is ERROR`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.ERROR
            HsLog.error("err msg")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("ERROR"))
            assertTrue(messages[0].contains("err msg"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `fatal message is emitted when level is FATAL`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.FATAL
            HsLog.fatal("fatal msg")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("FATAL"))
            assertTrue(messages[0].contains("fatal msg"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `info message is suppressed when level is ERROR`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.ERROR
            HsLog.info("suppressed")
            assertEquals(0, messages.size)
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `message formatting replaces single placeholder`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.info("hello {}", "world")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("hello world"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `message formatting replaces multiple placeholders`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.info("{} is {} years old", "Alice", 30)
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("Alice is 30 years old"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `extra args are appended when no placeholders`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.info("no placeholder", "extra1", "extra2")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("no placeholder"))
            assertTrue(messages[0].contains("extra1"))
            assertTrue(messages[0].contains("extra2"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `missing args leave placeholder text intact`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.info("{} and {} and {}", "one")
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("one"))
            assertTrue(messages[0].contains("{} and {}"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `null arg is rendered as null`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.info("value={}", null)
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("value=null"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `error with throwable includes message`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.ERROR
            HsLog.error("failure", RuntimeException("boom"))
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("failure"))
            assertTrue(messages[0].contains("boom"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `fatal with throwable includes message`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.FATAL
            HsLog.fatal("critical", IllegalStateException("crash"))
            assertEquals(1, messages.size)
            assertTrue(messages[0].contains("critical"))
            assertTrue(messages[0].contains("crash"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isDebugEnabled returns true when level is DEBUG`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            assertTrue(HsLog.isDebugEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isDebugEnabled returns false when level is INFO`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.INFO
            assertFalse(HsLog.isDebugEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isInfoEnabled returns true when level is DEBUG`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            assertTrue(HsLog.isInfoEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isInfoEnabled returns false when level is WARNING`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.WARNING
            assertFalse(HsLog.isInfoEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isWarnEnabled returns true at WARNING level`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.WARNING
            assertTrue(HsLog.isWarnEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `isErrorEnabled returns true at ERROR level`() {
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.currentLevel = HsLog.LogLevel.ERROR
            assertTrue(HsLog.isErrorEnabled())
        } finally {
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `custom output handler receives formatted messages`() {
        val received = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { received.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.DEBUG
            HsLog.debug("test msg")
            assertEquals(1, received.size)
            assertTrue(received[0].contains("DEBUG"))
            assertTrue(received[0].contains("test msg"))
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }

    @Test
    public fun `log level order is DEBUG INFO WARNING ERROR FATAL`() {
        val levels = HsLog.LogLevel.entries
        assertEquals(5, levels.size)
        assertEquals(HsLog.LogLevel.DEBUG, levels[0])
        assertEquals(HsLog.LogLevel.INFO, levels[1])
        assertEquals(HsLog.LogLevel.WARNING, levels[2])
        assertEquals(HsLog.LogLevel.ERROR, levels[3])
        assertEquals(HsLog.LogLevel.FATAL, levels[4])
    }

    @Test
    public fun `all levels above current level are emitted`() {
        val messages = mutableListOf<String>()
        val originalOutput = HsLog.output
        val originalLevel = HsLog.currentLevel
        try {
            HsLog.output = { messages.add(it) }
            HsLog.currentLevel = HsLog.LogLevel.WARNING
            HsLog.warn("w")
            HsLog.error("e")
            HsLog.fatal("f")
            assertEquals(3, messages.size)
        } finally {
            HsLog.output = originalOutput
            HsLog.currentLevel = originalLevel
        }
    }
}
