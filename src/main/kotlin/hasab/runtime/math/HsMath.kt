package hasab.runtime.math

import kotlin.math.*

/**
 * Math utilities providing constants and operations for both integer and floating-point arithmetic.
 */
public object HsMath {

    public val PI: Double = Math.PI
    public val E: Double = Math.E
    public val LN2: Double = Math.log(2.0)
    public val LN10: Double = Math.log(10.0)
    public val LOG2E: Double = Math.log(Math.E) / Math.log(2.0)
    public val LOG10E: Double = Math.log10(Math.E)
    public val SQRT2: Double = Math.sqrt(2.0)
    public val SQRT1_2: Double = Math.sqrt(0.5)

    public fun abs(x: Int): Int = kotlin.math.abs(x)

    public fun max(a: Int, b: Int): Int = kotlin.math.max(a, b)

    public fun min(a: Int, b: Int): Int = kotlin.math.min(a, b)

    public fun pow(base: Int, exponent: Int): Int {
        require(exponent >= 0) { "Exponent must be non-negative" }
        var result = 1
        var b = base
        var e = exponent
        while (e > 0) {
            if (e and 1 == 1) result *= b
            b *= b
            e = e shr 1
        }
        return result
    }

    public fun sqrt(x: Int): Int = floor(kotlin.math.sqrt(x.toDouble())).toInt()

    public fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    public fun lcm(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return kotlin.math.abs(a) / gcd(a, b) * kotlin.math.abs(b)
    }

    public fun clamp(value: Int, min: Int, max: Int): Int = kotlin.math.max(min, kotlin.math.min(max, value))

    public fun floorDiv(a: Int, b: Int): Int {
        var r = a / b
        if ((a xor b) < 0 && r * b != a) r--
        return r
    }

    public fun floorMod(a: Int, b: Int): Int {
        var r = a % b
        if ((a xor b) < 0 && r != 0) r += b
        return r
    }

    public fun randomInt(min: Int, max: Int): Int {
        require(min <= max) { "min must be <= max" }
        return (Math.random() * (max - min + 1)).toInt() + min
    }

    public fun abs(x: Double): Double = kotlin.math.abs(x)

    public fun max(a: Double, b: Double): Double = kotlin.math.max(a, b)

    public fun min(a: Double, b: Double): Double = kotlin.math.min(a, b)

    public fun pow(base: Double, exponent: Double): Double = base.pow(exponent)

    public fun sqrt(x: Double): Double = kotlin.math.sqrt(x)

    public fun cbrt(x: Double): Double = kotlin.math.cbrt(x)

    public fun sin(x: Double): Double = kotlin.math.sin(x)

    public fun cos(x: Double): Double = kotlin.math.cos(x)

    public fun tan(x: Double): Double = kotlin.math.tan(x)

    public fun asin(x: Double): Double = kotlin.math.asin(x)

    public fun acos(x: Double): Double = kotlin.math.acos(x)

    public fun atan(x: Double): Double = kotlin.math.atan(x)

    public fun atan2(y: Double, x: Double): Double = kotlin.math.atan2(y, x)

    public fun sinh(x: Double): Double = kotlin.math.sinh(x)

    public fun cosh(x: Double): Double = kotlin.math.cosh(x)

    public fun tanh(x: Double): Double = kotlin.math.tanh(x)

    public fun log(x: Double): Double = kotlin.math.ln(x)

    public fun log2(x: Double): Double = kotlin.math.log2(x)

    public fun log10(x: Double): Double = kotlin.math.log10(x)

    public fun exp(x: Double): Double = kotlin.math.exp(x)

    public fun exp2(x: Double): Double = 2.0.pow(x)

    public fun ceil(x: Double): Double = kotlin.math.ceil(x)

    public fun floor(x: Double): Double = kotlin.math.floor(x)

    public fun round(x: Double): Long = kotlin.math.round(x).toLong()

    public fun roundToDouble(x: Float): Double = x.toDouble()

    public fun sign(x: Double): Double = kotlin.math.sign(x)

    public fun toDegrees(radians: Double): Double = Math.toDegrees(radians)

    public fun toRadians(degrees: Double): Double = Math.toRadians(degrees)

    public fun hypot(x: Double, y: Double): Double = kotlin.math.hypot(x, y)

    public fun random(): Double = Math.random()

    public fun randomDouble(min: Double, max: Double): Double {
        require(min <= max) { "min must be <= max" }
        return Math.random() * (max - min) + min
    }

    public fun clamp(value: Double, min: Double, max: Double): Double = kotlin.math.max(min, kotlin.math.min(max, value))
}
