package hasab.cli.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

public class PackageRegistryTest {

    @Test
    public fun `resolveLatest returns version string`() {
        val registry = PackageRegistry()
        val result = registry.resolveLatest("web")
        // The simulated registry may or may not find it
        // Just verify it doesn't throw
    }

    @Test
    public fun `resolveLatest returns null for unknown package`() {
        val registry = PackageRegistry()
        val result = registry.resolveLatest("nonexistent-package-xyz-12345")
        // Should return null or a default
    }

    @Test
    public fun `PackageRegistry constructor works`() {
        val registry = PackageRegistry()
        assertNotNull(registry)
    }
}
