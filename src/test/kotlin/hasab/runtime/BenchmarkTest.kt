package hasab.runtime

import hasab.runtime.core.HsArray
import hasab.runtime.core.HsString
import hasab.runtime.collections.HsList
import hasab.runtime.collections.HsMutableList
import hasab.runtime.collections.HsMap
import hasab.runtime.collections.HsMutableMap
import hasab.runtime.collections.HsSet
import hasab.runtime.collections.HsMutableSet
import hasab.runtime.math.HsMath
import hasab.runtime.text.HsText
import hasab.runtime.io.HsFile
import kotlin.test.Test
import kotlin.test.assertTrue

class BenchmarkTest {

    private fun measure(block: () -> Unit): Long {
        val start = System.nanoTime()
        block()
        return System.nanoTime() - start
    }

    @Test
    fun `benchmark HsList creation`() {
        val iterations = 10_000
        val elapsed = measure {
            repeat(iterations) {
                HsList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            }
        }
        val perOp = elapsed / iterations
        assertTrue(perOp < 1_000_000, "HsList creation too slow: ${perOp}ns per op")
    }

    @Test
    fun `benchmark HsMutableList add`() {
        val list = HsMutableList.empty()
        val count = 100_000
        val elapsed = measure {
            repeat(count) { list.add(it) }
        }
        val perOp = elapsed / count
        assertTrue(perOp < 10_000, "HsMutableList.add too slow: ${perOp}ns per op")
    }

    @Test
    fun `benchmark HsMutableList get`() {
        val list = HsMutableList.empty()
        repeat(10_000) { list.add(it) }
        val count = 100_000
        val elapsed = measure {
            repeat(count) { list.get(it % list.size()) }
        }
        val perOp = elapsed / count
        assertTrue(perOp < 5_000, "HsMutableList.get too slow: ${perOp}ns per op")
    }

    @Test
    fun `benchmark HsMutableMap put get`() {
        val map = HsMutableMap.empty()
        val count = 10_000
        val elapsed = measure {
            repeat(count) { map.put("key$it", it) }
        }
        val perOp = elapsed / count
        assertTrue(perOp < 50_000, "HsMutableMap.put too slow: ${perOp}ns per op")

        val getElapsed = measure {
            repeat(count) { map.get("key${it % count}") }
        }
        val getPerOp = getElapsed / count
        assertTrue(getPerOp < 50_000, "HsMutableMap.get too slow: ${getPerOp}ns per op")
    }

    @Test
    fun `benchmark HsMutableSet add`() {
        val set = HsMutableSet.empty()
        val count = 10_000
        val elapsed = measure {
            repeat(count) { set.add(it) }
        }
        val perOp = elapsed / count
        assertTrue(perOp < 50_000, "HsMutableSet.add too slow: ${perOp}ns per op")
    }

    @Test
    fun `benchmark HsText operations`() {
        val text = "The quick brown fox jumps over the lazy dog ".repeat(100)
        val upperElapsed = measure {
            repeat(1_000) { HsText.upper(text) }
        }
        assertTrue(upperElapsed / 1_000 < 1_000_000, "HsText.upper too slow")

        val splitElapsed = measure {
            repeat(1_000) { HsText.split(text, " ") }
        }
        assertTrue(splitElapsed / 1_000 < 5_000_000, "HsText.split too slow")

        val replaceElapsed = measure {
            repeat(1_000) { HsText.replace(text, "fox", "cat") }
        }
        assertTrue(replaceElapsed / 1_000 < 5_000_000, "HsText.replace too slow")

        val containsElapsed = measure {
            repeat(1_000) { HsText.contains(text, "brown") }
        }
        assertTrue(containsElapsed / 1_000 < 1_000_000, "HsText.contains too slow")
    }

    @Test
    fun `benchmark HsString operations`() {
        val s = "hello world test string benchmark"
        val iterations = 100_000

        val lengthElapsed = measure {
            repeat(iterations) { HsString.length(s) }
        }
        assertTrue(lengthElapsed / iterations < 1_000, "HsString.length too slow")

        val upperElapsed = measure {
            repeat(iterations) { HsString.toUpperCase(s) }
        }
        assertTrue(upperElapsed / iterations < 50_000, "HsString.toUpperCase too slow")

        val containsElapsed = measure {
            repeat(iterations) { HsString.contains(s, "world") }
        }
        assertTrue(containsElapsed / iterations < 50_000, "HsString.contains too slow")
    }

    @Test
    fun `benchmark HsMath operations`() {
        val iterations = 100_000

        val powElapsed = measure {
            repeat(iterations) { HsMath.pow(2, 10) }
        }
        assertTrue(powElapsed / iterations < 50_000, "HsMath.pow too slow")

        val gcdElapsed = measure {
            repeat(iterations) { HsMath.gcd(123456, 789012) }
        }
        assertTrue(gcdElapsed / iterations < 100_000, "HsMath.gcd too slow")

        val sqrtElapsed = measure {
            repeat(iterations) { HsMath.sqrt(1000000) }
        }
        assertTrue(sqrtElapsed / iterations < 50_000, "HsMath.sqrt too slow")
    }

    @Test
    fun `benchmark HsArray operations`() {
        val size = 10_000
        val iterations = 100

        val createElapsed = measure {
            repeat(iterations) { HsArray.create(size) { it } }
        }
        assertTrue(createElapsed / iterations < 10_000_000, "HsArray.create too slow")

        val arr = HsArray.create(size) { it }
        val sortElapsed = measure {
            repeat(iterations / 10) {
                val copy = HsArray.copyOf(arr)
                HsArray.sort(copy)
            }
        }
        assertTrue(sortElapsed / (iterations / 10) < 100_000_000, "HsArray.sort too slow")
    }

    @Test
    fun `benchmark HsFile write read`() {
        val content = "benchmark line\n".repeat(1000)
        val iterations = 50

        val writeElapsed = measure {
            repeat(iterations) {
                val tmp = HsFile.tempFile(prefix = "bench")
                tmp.writeAllText(content)
                tmp.readAllText()
                tmp.delete()
            }
        }
        assertTrue(writeElapsed / iterations < 100_000_000, "HsFile write/read too slow")
    }

    @Test
    fun `benchmark HsText levenshtein`() {
        val a = "algorithm"
        val b = "altruistic"
        val iterations = 10_000
        val elapsed = measure {
            repeat(iterations) { HsText.levenshteinDistance(a, b) }
        }
        val perOp = elapsed / iterations
        assertTrue(perOp < 500_000, "HsText.levenshteinDistance too slow: ${perOp}ns per op")
    }

    @Test
    fun `benchmark HsText HTML escape`() {
        val text = "<div class=\"test\">Hello & World</div>".repeat(100)
        val iterations = 10_000
        val elapsed = measure {
            repeat(iterations) { HsText.escapeHtml(text) }
        }
        val perOp = elapsed / iterations
        assertTrue(perOp < 1_000_000, "HsText.escapeHtml too slow: ${perOp}ns per op")
    }
}
