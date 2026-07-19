package hasab.runtime.core

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

public class HsFloatTest {

    @Test
    public fun `constant values are correct`() {
        assertEquals(Float.MAX_VALUE, HsFloat.MAX_VALUE)
        assertEquals(Float.MIN_VALUE, HsFloat.MIN_VALUE)
        assertEquals(Float.POSITIVE_INFINITY, HsFloat.POSITIVE_INFINITY)
        assertEquals(Float.NEGATIVE_INFINITY, HsFloat.NEGATIVE_INFINITY)
        assertTrue(HsFloat.NaN.isNaN())
    }

    @Test
    public fun `toString converts float to string`() {
        assertEquals("0.0", HsFloat.toString(0.0f))
        assertEquals("3.14", HsFloat.toString(3.14f))
        assertEquals("-1.5", HsFloat.toString(-1.5f))
    }

    @Test
    public fun `parseFloat parses valid floats`() {
        assertEquals(3.14f, HsFloat.parseFloat("3.14"))
        assertEquals(-1.5f, HsFloat.parseFloat("-1.5"))
        assertEquals(0.0f, HsFloat.parseFloat("0"))
        assertEquals(100.0f, HsFloat.parseFloat("100"))
    }

    @Test
    public fun `parseFloat throws on invalid input`() {
        assertFailsWith<NumberFormatException> { HsFloat.parseFloat("abc") }
        assertFailsWith<NumberFormatException> { HsFloat.parseFloat("") }
    }

    @Test
    public fun `parseFloatOrNull returns null on invalid input`() {
        assertNull(HsFloat.parseFloatOrNull("abc"))
        assertNull(HsFloat.parseFloatOrNull(""))
    }

    @Test
    public fun `parseFloatOrNull returns value on valid input`() {
        assertEquals(3.14f, HsFloat.parseFloatOrNull("3.14"))
        assertEquals(-7.0f, HsFloat.parseFloatOrNull("-7"))
    }

    @Test
    public fun `toInt truncates toward zero`() {
        assertEquals(3, HsFloat.toInt(3.9f))
        assertEquals(3, HsFloat.toInt(3.1f))
        assertEquals(-3, HsFloat.toInt(-3.9f))
        assertEquals(0, HsFloat.toInt(0.5f))
    }

    @Test
    public fun `isNaN detects NaN`() {
        assertTrue(HsFloat.isNaN(Float.NaN))
        assertFalse(HsFloat.isNaN(0.0f))
        assertFalse(HsFloat.isNaN(Float.POSITIVE_INFINITY))
    }

    @Test
    public fun `isInfinite detects infinity`() {
        assertTrue(HsFloat.isInfinite(Float.POSITIVE_INFINITY))
        assertTrue(HsFloat.isInfinite(Float.NEGATIVE_INFINITY))
        assertFalse(HsFloat.isInfinite(0.0f))
        assertFalse(HsFloat.isInfinite(Float.NaN))
    }

    @Test
    public fun `isFinite detects finite values`() {
        assertTrue(HsFloat.isFinite(0.0f))
        assertTrue(HsFloat.isFinite(3.14f))
        assertFalse(HsFloat.isFinite(Float.POSITIVE_INFINITY))
        assertFalse(HsFloat.isFinite(Float.NEGATIVE_INFINITY))
        assertFalse(HsFloat.isFinite(Float.NaN))
    }

    @Test
    public fun `compare returns correct ordering`() {
        assertTrue(HsFloat.compare(1.0f, 2.0f) < 0)
        assertEquals(0, HsFloat.compare(5.0f, 5.0f))
        assertTrue(HsFloat.compare(10.0f, 3.0f) > 0)
    }

    @Test
    public fun `coerceIn clamps to range`() {
        assertEquals(5.0f, HsFloat.coerceIn(5.0f, 0.0f, 10.0f))
        assertEquals(0.0f, HsFloat.coerceIn(-5.0f, 0.0f, 10.0f))
        assertEquals(10.0f, HsFloat.coerceIn(15.0f, 0.0f, 10.0f))
    }

    @Test
    public fun `coerceAtLeast and coerceAtMost`() {
        assertEquals(10.0f, HsFloat.coerceAtLeast(5.0f, 10.0f))
        assertEquals(15.0f, HsFloat.coerceAtLeast(15.0f, 10.0f))
        assertEquals(5.0f, HsFloat.coerceAtMost(5.0f, 10.0f))
        assertEquals(10.0f, HsFloat.coerceAtMost(15.0f, 10.0f))
    }

    @Test
    public fun `parseFloat handles scientific notation`() {
        assertEquals(1.0E10f, HsFloat.parseFloat("1.0E10"))
        assertEquals(1.5E-3f, HsFloat.parseFloat("1.5E-3"))
    }
}
