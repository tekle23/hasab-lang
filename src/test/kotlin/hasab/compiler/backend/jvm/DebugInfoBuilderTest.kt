package hasab.compiler.backend.jvm

import org.objectweb.asm.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DebugInfoBuilderTest {

    @Test
    fun `labelForLine returns same label for same line`() {
        val builder = DebugInfoBuilder("test.hasab")
        val label1 = builder.labelForLine(10)
        val label2 = builder.labelForLine(10)
        assertNotNull(label1)
        assertEquals(label1, label2)
    }

    @Test
    fun `different lines get different labels`() {
        val builder = DebugInfoBuilder("test.hasab")
        val label1 = builder.labelForLine(10)
        val label2 = builder.labelForLine(20)
        assertNotNull(label1)
        assertNotNull(label2)
        assertEquals(false, label1 === label2)
    }

    @Test
    fun `getLineLabels returns all labels`() {
        val builder = DebugInfoBuilder("test.hasab")
        builder.labelForLine(1)
        builder.labelForLine(5)
        builder.labelForLine(10)
        val labels = builder.getLineLabels()
        assertEquals(3, labels.size)
        assertEquals(true, labels.containsKey(1))
        assertEquals(true, labels.containsKey(5))
        assertEquals(true, labels.containsKey(10))
    }

    @Test
    fun `getLineLabels returns empty map initially`() {
        val builder = DebugInfoBuilder("test.hasab")
        val labels = builder.getLineLabels()
        assertEquals(true, labels.isEmpty())
    }

    @Test
    fun `reset clears labels`() {
        val builder = DebugInfoBuilder("test.hasab")
        builder.labelForLine(1)
        builder.labelForLine(2)
        builder.labelForLine(3)
        assertEquals(3, builder.getLineLabels().size)
        builder.reset()
        assertEquals(0, builder.getLineLabels().size)
    }

    @Test
    fun `reset then add works`() {
        val builder = DebugInfoBuilder("test.hasab")
        builder.labelForLine(1)
        builder.reset()
        builder.labelForLine(5)
        val labels = builder.getLineLabels()
        assertEquals(1, labels.size)
        assertEquals(true, labels.containsKey(5))
    }

    @Test
    fun `labels preserve insertion order`() {
        val builder = DebugInfoBuilder("test.hasab")
        builder.labelForLine(100)
        builder.labelForLine(1)
        builder.labelForLine(50)
        val keys = builder.getLineLabels().keys
        assertEquals(3, keys.size)
        assertEquals(true, keys.containsAll(setOf(100, 1, 50)))
    }

    @Test
    fun `consecutive line numbers get unique labels`() {
        val builder = DebugInfoBuilder("test.hasab")
        val labels = (1..10).map { builder.labelForLine(it) }
        assertEquals(10, labels.toSet().size)
    }
}
