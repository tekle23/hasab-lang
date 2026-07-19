package hasab.runtime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class HsBoolTest {

    @Test
    public fun `toString converts true and false`() {
        assertEquals("true", HsBool.toString(true))
        assertEquals("false", HsBool.toString(false))
    }

    @Test
    public fun `parseBoolean parses correctly`() {
        assertTrue(HsBool.parseBoolean("true"))
        assertTrue(HsBool.parseBoolean("TRUE"))
        assertTrue(HsBool.parseBoolean("True"))
        assertFalse(HsBool.parseBoolean("false"))
        assertFalse(HsBool.parseBoolean("FALSE"))
        assertFalse(HsBool.parseBoolean("abc"))
        assertFalse(HsBool.parseBoolean(""))
    }

    @Test
    public fun `and returns true only when both true`() {
        assertTrue(HsBool.and(true, true))
        assertFalse(HsBool.and(true, false))
        assertFalse(HsBool.and(false, true))
        assertFalse(HsBool.and(false, false))
    }

    @Test
    public fun `or returns true when at least one is true`() {
        assertTrue(HsBool.or(true, true))
        assertTrue(HsBool.or(true, false))
        assertTrue(HsBool.or(false, true))
        assertFalse(HsBool.or(false, false))
    }

    @Test
    public fun `not negates value`() {
        assertFalse(HsBool.not(true))
        assertTrue(HsBool.not(false))
    }

    @Test
    public fun `xor returns true when exactly one is true`() {
        assertFalse(HsBool.xor(true, true))
        assertTrue(HsBool.xor(true, false))
        assertTrue(HsBool.xor(false, true))
        assertFalse(HsBool.xor(false, false))
    }

    @Test
    public fun `countTrue counts true values`() {
        assertEquals(0, HsBool.countTrue())
        assertEquals(1, HsBool.countTrue(false, true, false))
        assertEquals(3, HsBool.countTrue(true, true, true))
        assertEquals(2, HsBool.countTrue(true, false, true))
    }

    @Test
    public fun `countFalse counts false values`() {
        assertEquals(0, HsBool.countFalse())
        assertEquals(1, HsBool.countFalse(true, false, true))
        assertEquals(3, HsBool.countFalse(false, false, false))
        assertEquals(2, HsBool.countFalse(false, true, false))
    }

    @Test
    public fun `boolean logic table and and or`() {
        for (a in listOf(true, false)) {
            for (b in listOf(true, false)) {
                assertEquals(a && b, HsBool.and(a, b))
                assertEquals(a || b, HsBool.or(a, b))
                assertEquals(a xor b, HsBool.xor(a, b))
            }
        }
    }
}
