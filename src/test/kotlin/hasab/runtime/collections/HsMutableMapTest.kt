package hasab.runtime.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

public class HsMutableMapTest {

    @Test
    public fun `of creates mutable map with entries`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        assertEquals(2, map.size())
        assertEquals(1, map.get("a"))
        assertEquals(2, map.get("b"))
    }

    @Test
    public fun `empty creates empty mutable map`() {
        val map = HsMutableMap.empty()
        assertEquals(0, map.size())
        assertTrue(map.isEmpty())
    }

    @Test
    public fun `put adds new entry`() {
        val map = HsMutableMap.empty()
        assertNull(map.put("a", 1))
        assertEquals(1, map.size())
        assertEquals(1, map.get("a"))
    }

    @Test
    public fun `put replaces existing entry and returns old value`() {
        val map = HsMutableMap.of("a" to 1)
        val old = map.put("a", 99)
        assertEquals(1, old)
        assertEquals(99, map.get("a"))
        assertEquals(1, map.size())
    }

    @Test
    public fun `putAll copies all entries`() {
        val map = HsMutableMap.of("a" to 1)
        map.putAll(mapOf("b" to 2, "c" to 3))
        assertEquals(3, map.size())
        assertEquals(2, map.get("b"))
        assertEquals(3, map.get("c"))
    }

    @Test
    public fun `remove deletes entry and returns old value`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        val removed = map.remove("a")
        assertEquals(1, removed)
        assertEquals(1, map.size())
        assertFalse(map.containsKey("a"))
    }

    @Test
    public fun `remove returns null for missing key`() {
        val map = HsMutableMap.of("a" to 1)
        assertNull(map.remove("missing"))
        assertEquals(1, map.size())
    }

    @Test
    public fun `clear empties the map`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2, "c" to 3)
        map.clear()
        assertEquals(0, map.size())
        assertTrue(map.isEmpty())
    }

    @Test
    public fun `putIfAbsent inserts only when key is absent`() {
        val map = HsMutableMap.of("a" to 1)
        val existing = map.putIfAbsent("a", 99)
        assertEquals(1, existing)
        assertEquals(1, map.get("a"))

        assertNull(map.putIfAbsent("b", 2))
        assertEquals(2, map.get("b"))
        assertEquals(2, map.size())
    }

    @Test
    public fun `getOrPut computes value when absent`() {
        val map = HsMutableMap.of("a" to 1)
        val result1 = map.getOrPut("b") { 2 }
        assertEquals(2, result1)
        assertEquals(2, map.get("b"))

        val result2 = map.getOrPut("a") { 99 }
        assertEquals(1, result2)
        assertEquals(1, map.get("a"))
    }

    @Test
    public fun `computeIfAbsent computes when key is absent`() {
        val map = HsMutableMap.of("a" to 1)
        val result = map.computeIfAbsent("b") { (it as String).length }
        assertEquals(1, result)
        assertEquals(1, map.get("b"))
    }

    @Test
    public fun `computeIfAbsent does not recompute when key exists`() {
        val map = HsMutableMap.of("a" to 1)
        val result = map.computeIfAbsent("a") { 99 }
        assertEquals(1, result)
        assertEquals(1, map.get("a"))
    }

    @Test
    public fun `computeIfPresent remaps existing entry`() {
        val map = HsMutableMap.of("a" to 10)
        val result = map.computeIfPresent("a") { _, v -> (v as Int) * 2 }
        assertEquals(20, result)
        assertEquals(20, map.get("a"))
    }

    @Test
    public fun `computeIfPresent removes entry when function returns null`() {
        val map = HsMutableMap.of("a" to 10)
        val result = map.computeIfPresent("a") { _, _ -> null }
        assertNull(result)
        assertFalse(map.containsKey("a"))
        assertEquals(0, map.size())
    }

    @Test
    public fun `computeIfPresent does nothing for missing key`() {
        val map = HsMutableMap.of("a" to 10)
        val result = map.computeIfPresent("z") { _, v -> (v as Int) * 2 }
        assertNull(result)
        assertEquals(1, map.size())
    }

    @Test
    public fun `filter returns HsMap not HsMutableMap`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2, "c" to 3)
        val filtered = map.filter { _, v -> (v as Int) > 1 }
        assertEquals(2, filtered.size())
        assertFalse(filtered.containsKey("a"))
    }

    @Test
    public fun `mapValues transforms values`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        val mapped = map.mapValues { (_, v) -> (v as Int) * 10 }
        assertEquals(10, mapped.get("a"))
        assertEquals(20, mapped.get("b"))
    }

    @Test
    public fun `mapKeys transforms keys`() {
        val map = HsMutableMap.of("a" to 1)
        val mapped = map.mapKeys { (k, _) -> "${k}!" }
        assertEquals(1, mapped.get("a!"))
    }

    @Test
    public fun `plus returns HsMap`() {
        val map = HsMutableMap.of("a" to 1)
        val result = map.plus("b" to 2)
        assertEquals(2, result.size())
        assertEquals(2, result.get("b"))
    }

    @Test
    public fun `minus returns HsMap`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        val result = map.minus("a")
        assertEquals(1, result.size())
        assertFalse(result.containsKey("a"))
    }

    @Test
    public fun `toMutableMap returns independent copy`() {
        val map = HsMutableMap.of("a" to 1)
        val copy = map.toMutableMap()
        copy.put("b", 2)
        assertEquals(1, map.size())
        assertEquals(2, copy.size())
    }

    @Test
    public fun `any all none count work correctly`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2, "c" to 3)
        assertTrue(map.any { _, v -> (v as Int) > 2 })
        assertFalse(map.any { _, v -> (v as Int) > 10 })
        assertTrue(map.all { _, v -> (v as Int) > 0 })
        assertEquals(2, map.count { _, v -> (v as Int) > 1 })
    }

    @Test
    public fun `null keys and values are supported`() {
        val map = HsMutableMap.empty()
        map.put(null, "nothing")
        map.put("key", null)
        assertEquals(2, map.size())
        assertEquals("nothing", map.get(null))
        assertNull(map.get("key"))
    }

    @Test
    public fun `containsKey and containsValue work`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        assertTrue(map.containsKey("a"))
        assertFalse(map.containsKey("z"))
        assertTrue(map.containsValue(1))
        assertFalse(map.containsValue(99))
    }

    @Test
    public fun `forEach iterates all entries`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        val collected = mutableMapOf<Any?, Any?>()
        map.forEach { k, v -> collected[k] = v }
        assertEquals(2, collected.size)
        assertEquals(1, collected["a"])
    }

    @Test
    public fun `equals with HsMap`() {
        val mutable = HsMutableMap.of("a" to 1, "b" to 2)
        val immutable = HsMap.of("a" to 1, "b" to 2)
        assertTrue(mutable == immutable)
    }

    @Test
    public fun `javaMap exposes backing map`() {
        val map = HsMutableMap.of("a" to 1)
        assertEquals(1, map.javaMap.size)
        assertEquals(1, map.javaMap["a"])
    }

    @Test
    public fun `toString contains HsMutableMap prefix`() {
        val map = HsMutableMap.of("a" to 1)
        assertTrue(map.toString().startsWith("HsMutableMap("))
    }

    @Test
    public fun `removeAll equivalent via clear and re-add`() {
        val map = HsMutableMap.of("a" to 1, "b" to 2)
        map.clear()
        map.put("c", 3)
        assertEquals(1, map.size())
        assertEquals(3, map.get("c"))
        assertFalse(map.containsKey("a"))
    }
}
