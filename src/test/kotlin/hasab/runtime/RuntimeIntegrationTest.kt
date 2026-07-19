package hasab.runtime

import hasab.runtime.core.HsArray
import hasab.runtime.core.HsString
import hasab.runtime.collections.HsList
import hasab.runtime.collections.HsMutableList
import hasab.runtime.collections.HsMap
import hasab.runtime.collections.HsMutableMap
import hasab.runtime.collections.HsSet
import hasab.runtime.collections.HsMutableSet
import hasab.runtime.collections.HsStack
import hasab.runtime.collections.HsQueue
import hasab.runtime.math.HsMath
import hasab.runtime.text.HsText
import hasab.runtime.datetime.HsDateTime
import hasab.runtime.exceptions.HsRuntimeException
import hasab.runtime.exceptions.HsTypeException
import hasab.runtime.exceptions.HsValueError
import hasab.runtime.exceptions.HsIOError
import hasab.runtime.exceptions.HsNullPointerException
import hasab.runtime.exceptions.HsNotImplementedError
import hasab.runtime.exceptions.HsIndexOutOfBoundsException
import hasab.runtime.io.HsFile
import hasab.runtime.io.HsIO
import hasab.runtime.util.HsLog
import hasab.runtime.util.HsPlatform
import hasab.runtime.filesystem.HsPath
import hasab.runtime.filesystem.HsTempFile
import hasab.runtime.network.HsUrl
import hasab.runtime.network.HsHttpRequest
import hasab.runtime.services.HsVersion
import hasab.runtime.services.HsProfiler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

class RuntimeIntegrationTest {

    @Test
    fun `collections pipeline - filter map reduce`() {
        val list = HsList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val evens = list.filter { (it as Int) % 2 == 0 }
        assertEquals(5, evens.size())
    }

    @Test
    fun `mutable list operations`() {
        val list = HsMutableList.of("a", "b", "c")
        assertEquals(3, list.size())
        assertEquals("b", list.get(1))
        list.removeAt(1)
        assertEquals(2, list.size())
    }

    @Test
    fun `map put get contains`() {
        val map = HsMutableMap.of("one" to 1, "two" to 2)
        assertEquals(1, map.get("one"))
        assertTrue(map.containsKey("two"))
        assertFalse(map.containsKey("three"))
    }

    @Test
    fun `set operations`() {
        val set1 = HsSet.of(1, 2, 3)
        val set2 = HsSet.of(2, 3, 4)
        val union = set1.union(set2)
        assertEquals(4, union.size())
    }

    @Test
    fun `stack push pop`() {
        val stack = HsStack.of(1, 2, 3)
        assertEquals(3, stack.pop())
        assertEquals(2, stack.pop())
        assertEquals(1, stack.pop())
    }

    @Test
    fun `queue offer poll`() {
        val queue = HsQueue.of(1, 2, 3)
        assertEquals(1, queue.poll())
        assertEquals(2, queue.poll())
        assertEquals(3, queue.poll())
    }

    @Test
    fun `math operations on collection results`() {
        val numbers = HsList.of(1, 2, 3, 4, 5)
        val sum = numbers.fold(0) { acc, elem -> (acc as Int) + (elem as Int) }
        assertEquals(15, sum)
        val product = numbers.fold(1) { acc, elem -> (acc as Int) * (elem as Int) }
        assertEquals(120, product)
    }

    @Test
    fun `text processing pipeline`() {
        val words = HsList.of("hello", "world", "foo", "bar")
        val result = words.map { HsText.upper(it as String) }
        assertEquals("HELLO", result.get(0))
        assertEquals("WORLD", result.get(1))
    }

    @Test
    fun `string operations integration`() {
        val s = "Hello, World!"
        assertEquals(13, HsString.length(s))
        assertEquals("HELLO, WORLD!", HsString.toUpperCase(s))
        assertEquals("hello, world!", HsString.toLowerCase(s))
        assertTrue(HsString.contains(s, "World"))
        assertEquals(7, HsString.indexOf(s, "World"))
    }

    @Test
    fun `math integration with core types`() {
        val x = 3
        val y = 4
        assertEquals(81, HsMath.pow(x, y))
        assertEquals(5, HsMath.gcd(10, 15))
        assertEquals(30, HsMath.lcm(6, 15))
    }

    @Test
    fun `datetime integration`() {
        val now = HsDateTime.now()
        assertNotNull(now)
        val today = HsDateTime.today()
        assertNotNull(today)
        assertTrue(HsDateTime.isLeapYear(2024))
        assertFalse(HsDateTime.isLeapYear(2023))
    }

    @Test
    fun `text and collections integration`() {
        val text = "hello world foo bar baz"
        val words = HsText.split(text, " ")
        assertEquals(5, words.size)
        val upperWords = words.map { HsText.upper(it) }
        assertEquals("HELLO", upperWords[0])
    }

    @Test
    fun `exception hierarchy works`() {
        assertFailsWith<HsRuntimeException> {
            throw HsRuntimeException("test")
        }
        assertFailsWith<HsTypeException> {
            throw HsTypeException("expected Int, got String")
        }
        assertFailsWith<HsValueError> {
            throw HsValueError("invalid value")
        }
        assertFailsWith<HsIOError> {
            throw HsIOError("io failed")
        }
        assertFailsWith<HsNullPointerException> {
            throw HsNullPointerException("null reference")
        }
        assertFailsWith<HsNotImplementedError> {
            throw HsNotImplementedError("not implemented")
        }
        assertFailsWith<HsIndexOutOfBoundsException> {
            throw HsIndexOutOfBoundsException("index out of bounds")
        }
    }

    @Test
    fun `exception message propagation`() {
        val ex = assertFailsWith<HsRuntimeException> {
            throw HsRuntimeException("specific error")
        }
        assertEquals("specific error", ex.message)
    }

    @Test
    fun `exception cause chaining`() {
        val cause = HsRuntimeException("root cause")
        val ex = assertFailsWith<HsRuntimeException> {
            throw HsRuntimeException("wrapper", cause)
        }
        assertNotNull(ex.cause)
    }

    @Test
    fun `HsFile integration with text`() {
        val tmp = HsFile.tempFile(prefix = "hs_integration")
        try {
            tmp.writeAllText("Hello Integration")
            val content = tmp.readAllText()
            assertEquals("Hello Integration", content)
            val upper = HsText.upper(content)
            tmp.writeAllText(upper)
            assertEquals("HELLO INTEGRATION", tmp.readAllText())
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `HsFile line operations`() {
        val tmp = HsFile.tempFile(prefix = "hs_integration")
        try {
            tmp.writeAllText("line1\nline2\nline3")
            val lines = tmp.readAllLines()
            assertEquals(3, lines.size)
            assertEquals("line1", lines[0])
            assertEquals("line2", lines[1])
            assertEquals("line3", lines[2])
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `HsFile binary operations`() {
        val tmp = HsFile.tempFile(prefix = "hs_integration")
        try {
            val data = byteArrayOf(0, 1, 2, 3, 127)
            tmp.writeAllBytes(data)
            val readBack = tmp.readAllBytes()
            assertEquals(5, readBack.size)
            assertEquals(127.toByte(), readBack[4])
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun `array operations integration`() {
        val arr = HsArray.create(5) { it * 2 }
        assertEquals(5, HsArray.size(arr))
        assertEquals(0, HsArray.get(arr, 0))
        assertEquals(8, HsArray.get(arr, 4))
    }

    @Test
    fun `stream operations`() {
        val input = ByteArrayInputStream("hello stream".toByteArray())
        val output = ByteArrayOutputStream()
        val bytes = HsIO.copyStream(input, output)
        assertEquals(12, bytes)
        assertEquals("hello stream", output.toString())
    }

    @Test
    fun `text search and replace pipeline`() {
        val text = "the quick brown fox jumps over the lazy dog"
        val replaced = HsText.replace(text, "fox", "cat")
        assertTrue(replaced.contains("cat"))
        assertFalse(replaced.contains("fox"))
        val words = HsText.split(replaced, " ")
        assertEquals(9, words.size)
    }

    @Test
    fun `log level filtering`() {
        val messages = mutableListOf<String>()
        HsLog.output = { messages.add(it) }
        HsLog.currentLevel = HsLog.LogLevel.WARNING
        HsLog.debug("should not appear")
        HsLog.info("should not appear")
        HsLog.warn("should appear")
        HsLog.error("should also appear")
        assertEquals(2, messages.size)
        HsLog.currentLevel = HsLog.LogLevel.INFO
    }

    @Test
    fun `platform detection`() {
        val os = HsPlatform.osName
        assertTrue(os.isNotEmpty())
        assertTrue(HsPlatform.availableProcessors > 0)
        assertTrue(HsPlatform.maxMemory > 0)
    }

    @Test
    fun `version info`() {
        val version = HsVersion.version
        assertTrue(version.isNotEmpty())
    }

    @Test
    fun `profiler measures time`() {
        HsProfiler.resetAll()
        val timer = HsProfiler.timer("integration-test")
        Thread.sleep(10)
        timer.stop()
        assertTrue(timer.elapsedMillis() > 0)
        HsProfiler.resetAll()
    }

    @Test
    fun `text levenshtein distance`() {
        assertEquals(0, HsText.levenshteinDistance("hello", "hello"))
        assertEquals(3, HsText.levenshteinDistance("kitten", "sitting"))
    }

    @Test
    fun `text HTML escape unescape round trip`() {
        val original = "<div class=\"test\">Hello &amp; World</div>"
        val escaped = HsText.escapeHtml(original)
        assertTrue(escaped.contains("&lt;"))
        assertFalse(escaped.contains("<"))
    }

    @Test
    fun `text truncate`() {
        assertEquals("Hello, ...", HsText.truncate("Hello, World! This is long", 10))
        assertEquals("short", HsText.truncate("short", 10))
    }

    @Test
    fun `math constants are reasonable`() {
        assertTrue(HsMath.PI > 3.14 && HsMath.PI < 3.15)
        assertTrue(HsMath.E > 2.71 && HsMath.E < 2.72)
    }

    @Test
    fun `math trigonometric functions`() {
        assertEquals(0.0, HsMath.sin(0.0), 0.0001)
        assertEquals(1.0, HsMath.cos(0.0), 0.0001)
        assertEquals(0.0, HsMath.tan(0.0), 0.0001)
    }

    @Test
    fun `filesystem path utilities`() {
        val path = HsPath.normalize("foo//bar")
        assertTrue(path.contains("foo"))
        assertTrue(path.contains("bar"))
    }

    @Test
    fun `filesystem temp file lifecycle`() {
        val tmp = HsTempFile.create(prefix = "hs_lifecycle")
        tmp.writeText("lifecycle test")
        assertEquals("lifecycle test", tmp.readText())
        val hsFile = tmp.toHsFile()
        assertTrue(hsFile.exists)
        tmp.cleanup()
        assertFalse(tmp.exists)
    }

    @Test
    fun `array sort and reverse`() {
        val arr = HsArray.create(5) { (4 - it) }
        HsArray.sort(arr)
        assertEquals(0, HsArray.get(arr, 0))
        assertEquals(4, HsArray.get(arr, 4))
        HsArray.reverse(arr)
        assertEquals(4, HsArray.get(arr, 0))
        assertEquals(0, HsArray.get(arr, 4))
    }

    @Test
    fun `array map filter`() {
        val arr = HsArray.create(5) { it }
        val mapped = HsArray.map(arr) { (it as Int) * 10 }
        assertEquals(0, HsArray.get(mapped, 0))
        assertEquals(40, HsArray.get(mapped, 4))
        val filtered = HsArray.filter(arr) { (it as Int) > 2 }
        assertEquals(2, HsArray.size(filtered))
    }
}
