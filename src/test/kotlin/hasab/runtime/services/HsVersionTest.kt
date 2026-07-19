package hasab.runtime.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

public class HsVersionTest {

    @Test
    public fun `version string is in semver format`() {
        val parts = HsVersion.version.split('.')
        assertEquals(3, parts.size)
        assertTrue(parts.all { it.toIntOrNull() != null })
    }

    @Test
    public fun `majorVersion returns first segment`() {
        assertEquals(1, HsVersion.majorVersion())
    }

    @Test
    public fun `minorVersion returns second segment`() {
        assertEquals(0, HsVersion.minorVersion())
    }

    @Test
    public fun `patchVersion returns third segment`() {
        assertEquals(0, HsVersion.patchVersion())
    }

    @Test
    public fun `name is not empty`() {
        assertTrue(HsVersion.name.isNotEmpty())
    }

    @Test
    public fun `buildDate is not empty`() {
        assertTrue(HsVersion.buildDate.isNotEmpty())
    }

    @Test
    public fun `javaVersion is not empty`() {
        assertTrue(HsVersion.javaVersion.isNotEmpty())
    }

    @Test
    public fun `kotlinVersion is not empty`() {
        assertTrue(HsVersion.kotlinVersion.isNotEmpty())
    }

    @Test
    public fun `info returns multi-line string`() {
        val info = HsVersion.info()
        assertTrue(info.contains(HsVersion.name))
        assertTrue(info.contains(HsVersion.version))
        assertTrue(info.contains(HsVersion.buildDate))
    }

    @Test
    public fun `toString contains name and version`() {
        val str = HsVersion.toString()
        assertTrue(str.contains(HsVersion.name))
        assertTrue(str.contains(HsVersion.version))
    }

    @Test
    public fun `isCompatibleWith returns true for same version`() {
        assertTrue(HsVersion.isCompatibleWith("1.0.0"))
    }

    @Test
    public fun `isCompatibleWith returns true for lower version`() {
        assertTrue(HsVersion.isCompatibleWith("0.9.0"))
    }

    @Test
    public fun `isCompatibleWith returns true for lower patch version`() {
        assertTrue(HsVersion.isCompatibleWith("1.0.0"))
    }

    @Test
    public fun `isCompatibleWith returns false for higher major version`() {
        assertFalse(HsVersion.isCompatibleWith("2.0.0"))
    }

    @Test
    public fun `isCompatibleWith returns false for higher minor version`() {
        assertFalse(HsVersion.isCompatibleWith("1.1.0"))
    }

    @Test
    public fun `isCompatibleWith returns false for higher patch version`() {
        assertFalse(HsVersion.isCompatibleWith("1.0.1"))
    }

    @Test
    public fun `isCompatibleWith returns true for partial version`() {
        assertTrue(HsVersion.isCompatibleWith("1"))
    }

    @Test
    public fun `isCompatibleWith returns false for invalid version`() {
        assertFalse(HsVersion.isCompatibleWith("abc"))
    }

    @Test
    public fun `isCompatibleWith returns false for empty version`() {
        assertFalse(HsVersion.isCompatibleWith(""))
    }

    @Test
    public fun `isCompatibleWith with major only`() {
        assertTrue(HsVersion.isCompatibleWith("1"))
    }

    @Test
    public fun `isCompatibleWith with major only too high`() {
        assertFalse(HsVersion.isCompatibleWith("2"))
    }

    @Test
    public fun `isCompatibleWith with single digit minor`() {
        assertTrue(HsVersion.isCompatibleWith("1.0"))
    }

    @Test
    public fun `version string is 1_0_0`() {
        assertEquals("1.0.0", HsVersion.version)
    }
}
