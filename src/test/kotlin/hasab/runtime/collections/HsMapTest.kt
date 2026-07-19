package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsMapTest {

    @Test
    public fun `of creates map with entries`() {
        val map = HsMap.of("a" to 1, "b" to 2, "c" to 3)
        assertEquals(3, map.size())
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
    }

    @Test
    public fun `empty creates empty map`() {
        val map = HsMap.empty()
        assertEquals(0, map.size())
        assertTrue(map.isEmpty())
        assertFalse(map.isNotEmpty())
    }

    @Test
    public fun `get returns null for missing key`() {
        val map = HsMap.of("a" to 1)
        assertNull(map.get("missing"))
    }

    @Test
    public fun `getOrDefault returns default for missing key`() {
        val map = HsMap.of("a" to 1)
        assertEquals(99, map.getOrDefault("missing", 99))
        assertEquals(1, map.getOrDefault("a", 99))
    }

    @Test
    public fun `containsKey and containsValue work`() {
        val map = HsMap.of("a" to 1, "b" to 2)
        assertTrue(map.containsKey("a"))
        assertFalse(map.containsKey("z"))
        assertTrue(map.containsValue(1))
        assertFalse(map.containsValue(99))
    }

    @Test
    public fun `keys returns HsSet of keys`() {
        val map = HsMap.of("x" to 10, "y" to 20)
        val keys = map.keys()
        assertEquals(2, keys.size())
        assertTrue(keys.contains("x"))
        assertTrue(keys.contains("y"))
    }

    @Test
    public fun `values returns HsList of values`() {
        val map = HsMap.of("x" to 10, "y" to 20)
        val values = map.values()
        assertEquals(2, values.size())
        assertTrue(values.contains(10))
        assertTrue(values.contains(20))
    }

    @Test
    public fun `entries returns HsList of entries`() {
        val map = HsMap.of("a" to 1)
        val entries = map.entries()
        assertEquals(1, entries.size())
    }

    @Test
    public fun `filter keeps matching entries`() {
        val map = HsMap.of("a" to 1, "b" to 2, "c" to 3, "d" to 4)
        val filtered = map.filter { _, value -> (value as Int) > 2 }
        assertEquals(2, filtered.size())
        assertTrue(filtered.containsKey("c"))
        assertTrue(filtered.containsKey("d"))
        assertFalse(filtered.containsKey("a"))
    }

    @Test
    public fun `filterNot keeps non-matching entries`() {
        val map = HsMap.of("a" to 1, "b" to 2, "c" to 3)
        val filtered = map.filterNot { _, value -> (value as Int) > 1 }
        assertEquals(1, filtered.size())
        assertEquals(1, filtered.get("a"))
    }

    @Test
    public fun `filterKeys filters by key predicate`() {
        val map = HsMap.of("a" to 1, "bb" to 2, "ccc" to 3)
        val filtered = map.filterKeys { (it as String).length > 1 }
        assertEquals(2, filtered.size())
        assertFalse(filtered.containsKey("a"))
        assertTrue(filtered.containsKey("bb"))
        assertTrue(filtered.containsKey("ccc"))
    }

    @Test
    public fun `filterValues filters by value predicate`() {
        val map = HsMap.of("a" to 10, "b" to 20, "c" to 30)
        val filtered = map.filterValues { (it as Int) >= 20 }
        assertEquals(2, filtered.size())
        assertFalse(filtered.containsKey("a"))
        assertTrue(filtered.containsKey("b"))
        assertTrue(filtered.containsKey("c"))
    }

    @Test
    public fun `mapValues transforms values`() {
        val map = HsMap.of("a" to 1, "b" to 2)
        val mapped = map.mapValues { (_, value) -> (value as Int) * 10 }
        assertEquals(10, mapped.get("a"))
        assertEquals(20, mapped.get("b"))
    }

    @Test
    public fun `mapKeys transforms keys`() {
        val map = HsMap.of("a" to 1, "b" to 2)
        val mapped = map.mapKeys { (key, _) -> "${key}!" }
        assertEquals(1, mapped.get("a!"))
        assertEquals(2, mapped.get("b!"))
        assertNull(mapped.get("a"))
    }

    @Test
    public fun `any all none count work correctly`() {
        val map = HsMap.of("a" to 1, "b" to 2, "c" to 3)
        assertTrue(map.any { _, v -> (v as Int) > 2 })
        assertFalse(map.any { _, v -> (v as Int) > 10 })
        assertTrue(map.all { _, v -> (v as Int) > 0 })
        assertFalse(map.all { _, v -> (v as Int) > 2 })
        assertTrue(map.none { _, v -> (v as Int) > 10 })
        assertFalse(map.none { _, v -> (v as Int) > 2 })
        assertEquals(2, map.count { _, v -> (v as Int) > 1 })
    }

    @Test
    public fun `plus map merges with other taking precedence`() {
        val a = HsMap.of("a" to 1, "b" to 2)
        val b = HsMap.of("b" to 99, "c" to 3)
        val result = a.plus(b)
        assertEquals(3, result.size())
        assertEquals(1, result.get("a"))
        assertEquals(99, result.get("b"))
        assertEquals(3, result.get("c"))
    }

    @Test
    public fun `plus pair adds single entry`() {
        val map = HsMap.of("a" to 1)
        val result = map.plus("b" to 2)
        assertEquals(2, result.size())
        assertEquals(2, result.get("b"))
    }

    @Test
    public fun `minus key removes entry`() {
        val map = HsMap.of("a" to 1, "b" to 2, "c" to 3)
        val result = map.minus("b")
        assertEquals(2, result.size())
        assertNull(result.get("b"))
        assertEquals(1, result.get("a"))
    }

    @Test
    public fun `toMutableMap returns independent mutable copy`() {
        val map = HsMap.of("a" to 1)
        val mutable = map.toMutableMap()
        mutable.put("b", 2)
        assertEquals(1, map.size())
        assertEquals(2, mutable.size())
    }

    @Test
    public fun `equals and hashCode work`() {
        val a = HsMap.of("a" to 1, "b" to 2)
        val b = HsMap.of("a" to 1, "b" to 2)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    public fun `joinToString formats entries`() {
        val map = HsMap.of("a" to 1)
        val str = map.joinToString(", ")
        assertEquals("a=1", str)
    }

    @Test
    public fun `forEach iterates all entries`() {
        val map = HsMap.of("x" to 10, "y" to 20)
        val collected = mutableMapOf<Any?, Any?>()
        map.forEach { key, value -> collected[key] = value }
        assertEquals(2, collected.size)
        assertEquals(10, collected["x"])
        assertEquals(20, collected["y"])
    }

    @Test
    public fun `null keys and values are supported`() {
        val map = HsMap.of("a" to null, null to "b")
        assertEquals(2, map.size())
        assertNull(map.get("a"))
        assertEquals("b", map.get(null))
    }

    @Test
    public fun `fromJavaMap wraps existing map`() {
        val javaMap = mapOf<Any?, Any?>("x" to 1, "y" to 2)
        val hsMap = HsMap.fromJavaMap(javaMap)
        assertEquals(2, hsMap.size())
        assertEquals(1, hsMap.get("x"))
    }

    @Test
    public fun `javaMap exposes backing map`() {
        val map = HsMap.of("a" to 1, "b" to 2)
        assertEquals(2, map.javaMap.size)
        assertEquals(1, map.javaMap["a"])
    }

    @Test
    public fun `toString contains HsMap prefix`() {
        val map = HsMap.of("a" to 1)
        assertTrue(map.toString().startsWith("HsMap("))
    }
}
