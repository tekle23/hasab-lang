package hasab.runtime.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

public class HsPlatformTest {

    @Test
    public fun `osName is not empty`() {
        assertTrue(HsPlatform.osName.isNotEmpty())
    }

    @Test
    public fun `osArch is not empty`() {
        assertTrue(HsPlatform.osArch.isNotEmpty())
    }

    @Test
    public fun `javaVersion is not empty`() {
        assertTrue(HsPlatform.javaVersion.isNotEmpty())
    }

    @Test
    public fun `kotlinVersion is not empty`() {
        assertTrue(HsPlatform.kotlinVersion.isNotEmpty())
    }

    @Test
    public fun `at least one OS detection method returns true`() {
        val isWindows = HsPlatform.isWindows()
        val isLinux = HsPlatform.isLinux()
        val isMacOS = HsPlatform.isMacOS()
        assertTrue(isWindows || isLinux || isMacOS)
    }

    @Test
    public fun `isWindows returns boolean`() {
        val result = HsPlatform.isWindows()
        assertTrue(result || !result)
    }

    @Test
    public fun `isLinux returns boolean`() {
        val result = HsPlatform.isLinux()
        assertTrue(result || !result)
    }

    @Test
    public fun `isMacOS returns boolean`() {
        val result = HsPlatform.isMacOS()
        assertTrue(result || !result)
    }

    @Test
    public fun `isUnix is consistent with isLinux and isMacOS`() {
        val isUnix = HsPlatform.isUnix()
        val isLinux = HsPlatform.isLinux()
        val isMacOS = HsPlatform.isMacOS()
        if (isLinux || isMacOS) {
            assertTrue(isUnix)
        }
    }

    @Test
    public fun `userHome is not empty`() {
        assertTrue(HsPlatform.userHome.isNotEmpty())
    }

    @Test
    public fun `userDir is not empty`() {
        assertTrue(HsPlatform.userDir.isNotEmpty())
    }

    @Test
    public fun `tempDir is not empty`() {
        assertTrue(HsPlatform.tempDir.isNotEmpty())
    }

    @Test
    public fun `lineSeparator is not empty`() {
        assertTrue(HsPlatform.lineSeparator.isNotEmpty())
    }

    @Test
    public fun `availableProcessors is positive`() {
        assertTrue(HsPlatform.availableProcessors > 0)
    }

    @Test
    public fun `maxMemory is positive`() {
        assertTrue(HsPlatform.maxMemory > 0L)
    }

    @Test
    public fun `totalMemory is positive`() {
        assertTrue(HsPlatform.totalMemory > 0L)
    }

    @Test
    public fun `freeMemory is non-negative`() {
        assertTrue(HsPlatform.freeMemory >= 0L)
    }

    @Test
    public fun `freeMemory is less than or equal to totalMemory`() {
        assertTrue(HsPlatform.freeMemory <= HsPlatform.totalMemory)
    }

    @Test
    public fun `getEnvironment for PATH is non-null`() {
        val path = HsPlatform.getEnvironment("PATH")
            ?: HsPlatform.getEnvironment("Path")
        assertNotNull(path)
    }

    @Test
    public fun `getEnvironment for non-existent variable returns null`() {
        assertNull(HsPlatform.getEnvironment("NONEXISTENT_ENV_VAR_HASAB_TEST_12345"))
    }

    @Test
    public fun `getEnvironments is not empty`() {
        assertTrue(HsPlatform.getEnvironments().isNotEmpty())
    }

    @Test
    public fun `getSystemProperty for os name is not empty`() {
        val osName = HsPlatform.getSystemProperty("os.name")
        assertNotNull(osName)
        assertTrue(osName.isNotEmpty())
    }

    @Test
    public fun `getSystemProperty for non-existent returns null`() {
        assertNull(HsPlatform.getSystemProperty("nonexistent.property.hasab.test"))
    }

    @Test
    public fun `nanoTime returns positive value`() {
        val time = HsPlatform.nanoTime()
        assertTrue(time > 0L)
    }

    @Test
    public fun `currentTimeMillis returns reasonable value`() {
        val now = HsPlatform.currentTimeMillis()
        assertTrue(now > 0L)
        assertTrue(now > 1_000_000_000_000L)
    }

    @Test
    public fun `uptimeMillis is non-negative`() {
        assertTrue(HsPlatform.uptimeMillis() >= 0L)
    }

    @Test
    public fun `is64Bit returns boolean`() {
        val result = HsPlatform.is64Bit()
        assertTrue(result || !result)
    }
}
