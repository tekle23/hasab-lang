package hasab.runtime.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.math.abs

public class HsMathTest {

    // ── Constants ──────────────────────────────────────────────

    @Test
    public fun `PI is approximately 3 dot 14159`() {
        assertEquals(3.14159, HsMath.PI, 0.00001)
    }

    @Test
    public fun `E is approximately 2 dot 71828`() {
        assertEquals(2.71828, HsMath.E, 0.00001)
    }

    @Test
    public fun `LN2 is natural log of 2`() {
        assertEquals(kotlin.math.ln(2.0), HsMath.LN2, 1e-10)
    }

    @Test
    public fun `LN10 is natural log of 10`() {
        assertEquals(kotlin.math.ln(10.0), HsMath.LN10, 1e-10)
    }

    @Test
    public fun `SQRT2 is square root of 2`() {
        assertEquals(kotlin.math.sqrt(2.0), HsMath.SQRT2, 1e-10)
    }

    @Test
    public fun `SQRT1_2 is square root of 0 dot 5`() {
        assertEquals(kotlin.math.sqrt(0.5), HsMath.SQRT1_2, 1e-10)
    }

    // ── Integer abs, max, min ──────────────────────────────────

    @Test
    public fun `int abs of positive number`() {
        assertEquals(5, HsMath.abs(5))
    }

    @Test
    public fun `int abs of negative number`() {
        assertEquals(7, HsMath.abs(-7))
    }

    @Test
    public fun `int abs of zero`() {
        assertEquals(0, HsMath.abs(0))
    }

    @Test
    public fun `int max returns larger`() {
        assertEquals(10, HsMath.max(3, 10))
    }

    @Test
    public fun `int min returns smaller`() {
        assertEquals(3, HsMath.min(3, 10))
    }

    @Test
    public fun `int max when equal`() {
        assertEquals(5, HsMath.max(5, 5))
    }

    @Test
    public fun `int min when equal`() {
        assertEquals(5, HsMath.min(5, 5))
    }

    // ── Integer pow ────────────────────────────────────────────

    @Test
    public fun `int pow basic`() {
        assertEquals(8, HsMath.pow(2, 3))
    }

    @Test
    public fun `int pow exponent zero`() {
        assertEquals(1, HsMath.pow(5, 0))
    }

    @Test
    public fun `int pow base zero`() {
        assertEquals(0, HsMath.pow(0, 5))
    }

    @Test
    public fun `int pow negative base`() {
        assertEquals(-8, HsMath.pow(-2, 3))
    }

    @Test
    public fun `int pow negative base even exponent`() {
        assertEquals(16, HsMath.pow(-2, 4))
    }

    @Test
    public fun `int pow rejects negative exponent`() {
        assertFailsWith<IllegalArgumentException> {
            HsMath.pow(2, -1)
        }
    }

    // ── Integer sqrt ───────────────────────────────────────────

    @Test
    public fun `int sqrt perfect square`() {
        assertEquals(4, HsMath.sqrt(16))
    }

    @Test
    public fun `int sqrt floors result`() {
        assertEquals(3, HsMath.sqrt(10))
    }

    @Test
    public fun `int sqrt of zero`() {
        assertEquals(0, HsMath.sqrt(0))
    }

    @Test
    public fun `int sqrt of one`() {
        assertEquals(1, HsMath.sqrt(1))
    }

    // ── GCD and LCM ───────────────────────────────────────────

    @Test
    public fun `gcd of two positive numbers`() {
        assertEquals(6, HsMath.gcd(12, 18))
    }

    @Test
    public fun `gcd of coprime numbers`() {
        assertEquals(1, HsMath.gcd(7, 13))
    }

    @Test
    public fun `gcd with negative numbers`() {
        assertEquals(6, HsMath.gcd(-12, 18))
    }

    @Test
    public fun `gcd with zero`() {
        assertEquals(5, HsMath.gcd(5, 0))
    }

    @Test
    public fun `lcm of two numbers`() {
        assertEquals(12, HsMath.lcm(4, 6))
    }

    @Test
    public fun `lcm with zero returns zero`() {
        assertEquals(0, HsMath.lcm(5, 0))
    }

    @Test
    public fun `lcm of coprime numbers`() {
        assertEquals(35, HsMath.lcm(5, 7))
    }

    @Test
    public fun `lcm with negative numbers`() {
        assertEquals(12, HsMath.lcm(-4, 6))
    }

    // ── Clamp (Int) ────────────────────────────────────────────

    @Test
    public fun `int clamp within range`() {
        assertEquals(5, HsMath.clamp(5, 0, 10))
    }

    @Test
    public fun `int clamp below range`() {
        assertEquals(0, HsMath.clamp(-5, 0, 10))
    }

    @Test
    public fun `int clamp above range`() {
        assertEquals(10, HsMath.clamp(15, 0, 10))
    }

    @Test
    public fun `int clamp at boundaries`() {
        assertEquals(0, HsMath.clamp(0, 0, 10))
        assertEquals(10, HsMath.clamp(10, 0, 10))
    }

    // ── FloorDiv and FloorMod ──────────────────────────────────

    @Test
    public fun `floorDiv positive numbers`() {
        assertEquals(3, HsMath.floorDiv(7, 2))
    }

    @Test
    public fun `floorDiv negative numerator`() {
        assertEquals(-4, HsMath.floorDiv(-7, 2))
    }

    @Test
    public fun `floorMod positive numbers`() {
        assertEquals(1, HsMath.floorMod(7, 2))
    }

    @Test
    public fun `floorMod negative numerator`() {
        assertEquals(1, HsMath.floorMod(-7, 2))
    }

    // ── Double abs, max, min ───────────────────────────────────

    @Test
    public fun `double abs of negative`() {
        assertEquals(3.5, HsMath.abs(-3.5), 1e-10)
    }

    @Test
    public fun `double max`() {
        assertEquals(2.5, HsMath.max(1.0, 2.5), 1e-10)
    }

    @Test
    public fun `double min`() {
        assertEquals(1.0, HsMath.min(1.0, 2.5), 1e-10)
    }

    // ── Double pow and sqrt ────────────────────────────────────

    @Test
    public fun `double pow`() {
        assertEquals(8.0, HsMath.pow(2.0, 3.0), 1e-10)
    }

    @Test
    public fun `double sqrt`() {
        assertEquals(3.0, HsMath.sqrt(9.0), 1e-10)
    }

    @Test
    public fun `double sqrt of 2`() {
        assertEquals(kotlin.math.sqrt(2.0), HsMath.sqrt(2.0), 1e-10)
    }

    @Test
    public fun `double cbrt`() {
        assertEquals(3.0, HsMath.cbrt(27.0), 1e-10)
    }

    @Test
    public fun `double cbrt of negative`() {
        assertEquals(-2.0, HsMath.cbrt(-8.0), 1e-10)
    }

    // ── Trig functions ─────────────────────────────────────────

    @Test
    public fun `sin of zero`() {
        assertEquals(0.0, HsMath.sin(0.0), 1e-10)
    }

    @Test
    public fun `sin of pi over 2`() {
        assertEquals(1.0, HsMath.sin(HsMath.PI / 2), 1e-10)
    }

    @Test
    public fun `cos of zero`() {
        assertEquals(1.0, HsMath.cos(0.0), 1e-10)
    }

    @Test
    public fun `cos of pi`() {
        assertEquals(-1.0, HsMath.cos(HsMath.PI), 1e-10)
    }

    @Test
    public fun `tan of zero`() {
        assertEquals(0.0, HsMath.tan(0.0), 1e-10)
    }

    @Test
    public fun `tan of pi over 4`() {
        assertEquals(1.0, HsMath.tan(HsMath.PI / 4), 1e-10)
    }

    @Test
    public fun `asin of 1`() {
        assertEquals(HsMath.PI / 2, HsMath.asin(1.0), 1e-10)
    }

    @Test
    public fun `acos of 1`() {
        assertEquals(0.0, HsMath.acos(1.0), 1e-10)
    }

    @Test
    public fun `atan of 1`() {
        assertEquals(HsMath.PI / 4, HsMath.atan(1.0), 1e-10)
    }

    @Test
    public fun `atan2 of positive x and y`() {
        assertEquals(HsMath.PI / 4, HsMath.atan2(1.0, 1.0), 1e-10)
    }

    // ── Hyperbolic functions ───────────────────────────────────

    @Test
    public fun `sinh of zero`() {
        assertEquals(0.0, HsMath.sinh(0.0), 1e-10)
    }

    @Test
    public fun `cosh of zero`() {
        assertEquals(1.0, HsMath.cosh(0.0), 1e-10)
    }

    @Test
    public fun `tanh of zero`() {
        assertEquals(0.0, HsMath.tanh(0.0), 1e-10)
    }

    @Test
    public fun `tanh approaches 1 for large input`() {
        assertEquals(1.0, HsMath.tanh(100.0), 1e-10)
    }

    // ── Log and Exp ────────────────────────────────────────────

    @Test
    public fun `log of 1 is zero`() {
        assertEquals(0.0, HsMath.log(1.0), 1e-10)
    }

    @Test
    public fun `log of e is 1`() {
        assertEquals(1.0, HsMath.log(HsMath.E), 1e-10)
    }

    @Test
    public fun `log2 of 8 is 3`() {
        assertEquals(3.0, HsMath.log2(8.0), 1e-10)
    }

    @Test
    public fun `log10 of 100 is 2`() {
        assertEquals(2.0, HsMath.log10(100.0), 1e-10)
    }

    @Test
    public fun `exp of 0 is 1`() {
        assertEquals(1.0, HsMath.exp(0.0), 1e-10)
    }

    @Test
    public fun `exp of 1 is e`() {
        assertEquals(HsMath.E, HsMath.exp(1.0), 1e-10)
    }

    @Test
    public fun `exp2 of 3 is 8`() {
        assertEquals(8.0, HsMath.exp2(3.0), 1e-10)
    }

    // ── Ceil, Floor, Round ─────────────────────────────────────

    @Test
    public fun `ceil rounds up`() {
        assertEquals(3.0, HsMath.ceil(2.1), 1e-10)
    }

    @Test
    public fun `ceil of integer`() {
        assertEquals(3.0, HsMath.ceil(3.0), 1e-10)
    }

    @Test
    public fun `floor rounds down`() {
        assertEquals(2.0, HsMath.floor(2.9), 1e-10)
    }

    @Test
    public fun `floor of integer`() {
        assertEquals(3.0, HsMath.floor(3.0), 1e-10)
    }

    @Test
    public fun `round to nearest`() {
        assertEquals(2L, HsMath.round(2.5))
        assertEquals(2L, HsMath.round(2.4))
    }

    @Test
    public fun `round negative`() {
        assertEquals(-2L, HsMath.round(-2.5))
    }

    @Test
    public fun `roundToDouble`() {
        assertEquals(3.14, HsMath.roundToDouble(3.14f), 1e-5)
    }

    // ── Sign ───────────────────────────────────────────────────

    @Test
    public fun `sign of positive`() {
        assertEquals(1.0, HsMath.sign(5.0), 1e-10)
    }

    @Test
    public fun `sign of negative`() {
        assertEquals(-1.0, HsMath.sign(-5.0), 1e-10)
    }

    @Test
    public fun `sign of zero`() {
        assertEquals(0.0, HsMath.sign(0.0), 1e-10)
    }

    // ── Degrees / Radians ─────────────────────────────────────

    @Test
    public fun `toDegrees converts pi to 180`() {
        assertEquals(180.0, HsMath.toDegrees(HsMath.PI), 1e-10)
    }

    @Test
    public fun `toRadians converts 180 to pi`() {
        assertEquals(HsMath.PI, HsMath.toRadians(180.0), 1e-10)
    }

    // ── Hypot ──────────────────────────────────────────────────

    @Test
    public fun `hypot 3 and 4 is 5`() {
        assertEquals(5.0, HsMath.hypot(3.0, 4.0), 1e-10)
    }

    // ── Random ─────────────────────────────────────────────────

    @Test
    public fun `random is between 0 and 1`() {
        repeat(100) {
            val r = HsMath.random()
            assertTrue(r >= 0.0)
            assertTrue(r < 1.0)
        }
    }

    @Test
    public fun `randomInt is within range`() {
        repeat(200) {
            val r = HsMath.randomInt(5, 10)
            assertTrue(r in 5..10)
        }
    }

    @Test
    public fun `randomInt rejects min greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            HsMath.randomInt(10, 5)
        }
    }

    @Test
    public fun `randomDouble is within range`() {
        repeat(200) {
            val r = HsMath.randomDouble(2.0, 4.0)
            assertTrue(r >= 2.0)
            assertTrue(r < 4.0)
        }
    }

    @Test
    public fun `randomDouble rejects min greater than max`() {
        assertFailsWith<IllegalArgumentException> {
            HsMath.randomDouble(5.0, 2.0)
        }
    }

    // ── Double clamp ───────────────────────────────────────────

    @Test
    public fun `double clamp within range`() {
        assertEquals(5.5, HsMath.clamp(5.5, 0.0, 10.0), 1e-10)
    }

    @Test
    public fun `double clamp below range`() {
        assertEquals(0.0, HsMath.clamp(-3.0, 0.0, 10.0), 1e-10)
    }

    @Test
    public fun `double clamp above range`() {
        assertEquals(10.0, HsMath.clamp(15.0, 0.0, 10.0), 1e-10)
    }

    // ── Edge cases ─────────────────────────────────────────────

    @Test
    public fun `int pow handles large values`() {
        assertEquals(1024, HsMath.pow(2, 10))
    }

    @Test
    public fun `double pow negative exponent`() {
        assertEquals(0.25, HsMath.pow(2.0, -2.0), 1e-10)
    }

    @Test
    public fun `sqrt of large perfect square`() {
        assertEquals(1000, HsMath.sqrt(1_000_000))
    }

    @Test
    public fun `log of very small number is negative`() {
        assertTrue(HsMath.log(0.01) < 0)
    }

    @Test
    public fun `sin and cos satisfy identity`() {
        val x = 1.23
        val result = HsMath.sin(x) * HsMath.sin(x) + HsMath.cos(x) * HsMath.cos(x)
        assertEquals(1.0, result, 1e-10)
    }

    @Test
    public fun `exp and log are inverses`() {
        val x = 2.5
        assertEquals(x, HsMath.log(HsMath.exp(x)), 1e-10)
    }
}
