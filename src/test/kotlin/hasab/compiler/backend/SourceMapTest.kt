package hasab.compiler.backend

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SourceMapTest {

    @Test
    fun `record and translate compile error`() {
        val sm = SourceMap()
        sm.record("Main.java", 10, "main.has", 5, 2)

        val loc = sm.translateCompileError("Main.java", 10)
        assertNotNull(loc)
        assertEquals("main.has", loc.file)
        assertEquals(5, loc.line)
        assertEquals(2, loc.column)
    }

    @Test
    fun `translate compile error returns nearest line`() {
        val sm = SourceMap()
        sm.record("Main.java", 10, "main.has", 5, 0)
        sm.record("Main.java", 15, "main.has", 8, 0)

        val loc = sm.translateCompileError("Main.java", 12)
        assertNotNull(loc)
        assertEquals(5, loc.line)
    }

    @Test
    fun `translate compile error returns null for unknown file`() {
        val sm = SourceMap()
        assertNull(sm.translateCompileError("Unknown.java", 1))
    }

    @Test
    fun `translate compile error returns null for empty map`() {
        val sm = SourceMap()
        assertNull(sm.translateCompileError("Main.java", 1))
    }

    @Test
    fun `record class mapping and translate runtime trace`() {
        val sm = SourceMap()
        sm.recordClassMapping("Main", "main.has")

        val loc = sm.translateRuntimeTrace("Main", 5)
        assertNotNull(loc)
        assertEquals("main.has", loc.file)
        assertEquals(5, loc.line)
    }

    @Test
    fun `runtime trace with line offset`() {
        val sm = SourceMap()
        sm.recordSourceFileLineOffset("main.has", 10)
        sm.recordClassMapping("Main", "main.has")

        val loc = sm.translateRuntimeTrace("Main", 5)
        assertNotNull(loc)
        assertEquals(15, loc.line)
    }

    @Test
    fun `translate runtime trace returns null for unknown class`() {
        val sm = SourceMap()
        assertNull(sm.translateRuntimeTrace("Unknown", 1))
    }

    @Test
    fun `translate runtime trace all returns multiple candidates`() {
        val sm = SourceMap()
        sm.recordClassMapping("Main", "main.has")
        sm.record("Main.java", 10, "main.has", 5, 0)
        sm.record("Main.java", 12, "main.has", 7, 0)
        sm.record("Main.java", 20, "main.has", 15, 0)

        val candidates = sm.translateRuntimeTraceAll("Main", 5)
        assertTrue(candidates.size >= 1)
    }

    @Test
    fun `translate runtime trace all returns empty for unknown class`() {
        val sm = SourceMap()
        val candidates = sm.translateRuntimeTraceAll("Unknown", 1)
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `get generated to source map`() {
        val sm = SourceMap()
        sm.record("Main.java", 10, "main.has", 5, 2)
        sm.record("Main.java", 20, "main.has", 8, 4)

        val map = sm.getGeneratedToSourceMap("Main.java")
        assertEquals(2, map.size)
        assertNotNull(map[10])
        assertNotNull(map[20])
    }

    @Test
    fun `get generated to source map returns empty for unknown file`() {
        val sm = SourceMap()
        val map = sm.getGeneratedToSourceMap("Unknown.java")
        assertTrue(map.isEmpty())
    }

    @Test
    fun `size counts all entries`() {
        val sm = SourceMap()
        sm.record("Main.java", 1, "main.has", 1, 0)
        sm.record("Main.java", 2, "main.has", 2, 0)
        sm.record("Main.java", 3, "main.has", 3, 0)
        assertEquals(3, sm.size)
    }

    @Test
    fun `source location data class`() {
        val loc = SourceLocation("main.has", 5, 2, 5, 10)
        assertEquals("main.has", loc.file)
        assertEquals(5, loc.line)
        assertEquals(2, loc.column)
        assertEquals(5, loc.endLine)
        assertEquals(10, loc.endColumn)
    }

    @Test
    fun `source location default end values`() {
        val loc = SourceLocation("main.has", 5, 2)
        assertEquals(5, loc.endLine)
        assertEquals(2, loc.endColumn)
    }

    @Test
    fun `nearest line prefers exact match`() {
        val sm = SourceMap()
        sm.record("Main.java", 10, "main.has", 5, 0)
        sm.record("Main.java", 11, "main.has", 6, 0)

        val loc = sm.translateCompileError("Main.java", 11)
        assertNotNull(loc)
        assertEquals(6, loc.line)
    }

    @Test
    fun `record class mapping only maps one class`() {
        val sm = SourceMap()
        sm.recordClassMapping("Main", "main.has")
        sm.recordClassMapping("Helper", "helper.has")

        val loc1 = sm.translateRuntimeTrace("Main", 1)
        val loc2 = sm.translateRuntimeTrace("Helper", 1)
        assertNotNull(loc1)
        assertNotNull(loc2)
        assertEquals("main.has", loc1.file)
        assertEquals("helper.has", loc2.file)
    }
}
