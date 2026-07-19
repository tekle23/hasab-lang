package hasab.lsp.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LspLoggerTest {

    @Test
    public fun `debug info warn error log at correct levels`() {
        val logger = LspLogger(minLevel = LspLogger.Level.DEBUG)
        logger.debug("debug message")
        logger.info("info message")
        logger.warn("warn message")
        logger.error("error message")
        val logs = logger.getRecentLogs(100)
        assertEquals(4, logs.size)
        assertEquals(LspLogger.Level.DEBUG, logs[0].level)
        assertEquals("debug message", logs[0].message)
        assertEquals(LspLogger.Level.INFO, logs[1].level)
        assertEquals("info message", logs[1].message)
        assertEquals(LspLogger.Level.WARNING, logs[2].level)
        assertEquals("warn message", logs[2].message)
        assertEquals(LspLogger.Level.ERROR, logs[3].level)
        assertEquals("error message", logs[3].message)
    }

    @Test
    public fun `getRecentLogs returns recent entries`() {
        val logger = LspLogger(minLevel = LspLogger.Level.DEBUG)
        for (i in 1..10) {
            logger.info("message $i")
        }
        val recent = logger.getRecentLogs(3)
        assertEquals(3, recent.size)
        assertEquals("message 8", recent[0].message)
        assertEquals("message 9", recent[1].message)
        assertEquals("message 10", recent[2].message)
    }

    @Test
    public fun `getLogsByLevel filters correctly`() {
        val logger = LspLogger(minLevel = LspLogger.Level.DEBUG)
        logger.debug("d1")
        logger.info("i1")
        logger.warn("w1")
        logger.info("i2")
        logger.error("e1")
        val infoLogs = logger.getLogsByLevel(LspLogger.Level.INFO)
        assertEquals(2, infoLogs.size)
        assertTrue(infoLogs.all { it.level == LspLogger.Level.INFO })
        val warnLogs = logger.getLogsByLevel(LspLogger.Level.WARNING)
        assertEquals(1, warnLogs.size)
        val errorLogs = logger.getLogsByLevel(LspLogger.Level.ERROR)
        assertEquals(1, errorLogs.size)
    }

    @Test
    public fun `clear removes all entries`() {
        val logger = LspLogger(minLevel = LspLogger.Level.DEBUG)
        logger.info("message 1")
        logger.info("message 2")
        logger.info("message 3")
        assertEquals(3, logger.getRecentLogs(100).size)
        logger.clear()
        assertEquals(0, logger.getRecentLogs(100).size)
    }

    @Test
    public fun `onLog callback fires`() {
        val logger = LspLogger(minLevel = LspLogger.Level.DEBUG)
        var callbackCount = 0
        var lastEntry: LspLogger.LogEntry? = null
        logger.onLog = { entry ->
            callbackCount++
            lastEntry = entry
        }
        logger.info("callback test")
        assertEquals(1, callbackCount)
        assertNotNull(lastEntry)
        assertEquals("callback test", lastEntry!!.message)
        assertEquals(LspLogger.Level.INFO, lastEntry!!.level)
    }

    @Test
    public fun `minLevel filters messages below threshold`() {
        val logger = LspLogger(minLevel = LspLogger.Level.WARNING)
        logger.debug("should be filtered")
        logger.info("should be filtered")
        logger.warn("should be kept")
        logger.error("should be kept")
        val logs = logger.getRecentLogs(100)
        assertEquals(2, logs.size)
        assertTrue(logs.all { it.level >= LspLogger.Level.WARNING })
    }
}
