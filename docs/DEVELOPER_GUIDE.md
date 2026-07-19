# HASAB Runtime & Standard Library — Developer Guide

## Table of Contents

1. [Introduction & Overview](#1-introduction--overview)
2. [Quick Start](#2-quick-start)
3. [Module Reference](#3-module-reference)
4. [Configuration & Setup](#4-configuration--setup)
5. [Error Handling](#5-error-handling)
6. [Best Practices](#6-best-practices)
7. [Performance Tips](#7-performance-tips)
8. [Migration from JVM Standard Library](#8-migration-from-jvm-standard-library)
9. [API Stability Guarantees](#9-api-stability-guarantees)
10. [Future Roadmap](#10-future-roadmap)

---

## 1. Introduction & Overview

The HASAB Runtime is a Kotlin-based standard library that provides a complete execution environment for HASAB programs. It wraps JVM primitives and standard library types in HASAB-specific types, providing consistent error handling, type checking, and a rich set of utility APIs.

**Technology Stack:**

- Language: Kotlin 2.0.21 targeting JVM 21
- Build system: Gradle with `explicitApi()` mode (all public APIs are explicitly declared)
- Bytecode: ASM 9.7.1 (for compiler backend)
- Reflection: kotlin-reflect

**Package Structure:**

| Package | Purpose |
|---------|---------|
| `hasab.runtime.core` | Primitive wrappers (`HsObject`, `HsString`, `HsInt`, etc.) |
| `hasab.runtime.collections` | Immutable & mutable collections |
| `hasab.runtime.io` | Console I/O and file operations |
| `hasab.runtime.text` | Advanced text processing |
| `hasab.runtime.math` | Math constants and functions |
| `hasab.runtime.datetime` | Date/time parsing, formatting, arithmetic |
| `hasab.runtime.concurrency` | Threads, locks, futures, executors |
| `hasab.runtime.filesystem` | Path utilities, directory ops, file watchers |
| `hasab.runtime.network` | URL parsing, HTTP client |
| `hasab.runtime.exceptions` | HASAB exception hierarchy |
| `hasab.runtime.reflection` | Runtime reflection utilities |
| `hasab.runtime.annotations` | Meta-annotations for HASAB semantics |
| `hasab.runtime.services` | Version info and profiling |
| `hasab.runtime.util` | Platform detection, logging, config, localization |
| `hasab.runtime.internal` | PublishedApi internal helpers (not for user code) |

---

## 2. Quick Start

### Hello World

```kotlin
import hasab.runtime.io.HsIO

fun main() {
    HsIO.println("Hello, HASAB!")
}
```

### Working with Collections

```kotlin
import hasab.runtime.collections.HsList
import hasab.runtime.collections.HsMap

fun main() {
    val list = HsList.of(1, 2, 3, 4, 5)
    val doubled = list.map { (it as Int) * 2 }
    HsIO.println(doubled.joinToString(", "))  // 2, 4, 6, 8, 10

    val scores = HsMap.of("alice" to 95, "bob" to 87, "carol" to 92)
    val topScorers = scores.filter { _, v -> (v as Int) > 90 }
    HsIO.println(topScorers)  // HsMap({alice=95, carol=92})
}
```

### HTTP Requests

```kotlin
import hasab.runtime.network.HsHttpClient
import hasab.runtime.network.HsHttpRequest

fun main() {
    val client = HsHttpClient()
    val response = client.get("https://api.example.com/status")
    if (response.isSuccessful) {
        HsIO.println("Status: ${response.statusCode}")
        HsIO.println("Body: ${response.bodyAsString()}")
    }
}
```

### Error Handling

```kotlin
import hasab.runtime.exceptions.*
import hasab.runtime.io.HsIO

fun divide(a: Int, b: Int): Int {
    if (b == 0) throw HsDivisionByZeroError("Cannot divide $a by zero")
    return a / b
}

fun main() {
    try {
        HsIO.println(divide(10, 0))
    } catch (e: HsException) {
        HsIO.println("Error: ${e.hsMessage}")
    }
}
```

---

## 3. Module Reference

### 3.1 core/ — Primitive Wrappers

#### HsObject

Base class for all HASAB runtime objects. Provides identity-based equality and hashing.

```kotlin
import hasab.runtime.core.HsObject

val obj = HsObject()
obj.typeName()              // "Object"
obj.identityHashCode()      // JVM identity hash code
obj.toString()              // "Object@7c3df462"
```

`HsObject` uses `===` (reference identity) for `equals()` and `System.identityHashCode()` for `hashCode()`. Subclasses should override `typeName()` to return the HASAB type name.

#### HsString

Static string utilities. All methods accept and return JVM `String` instances.

```kotlin
import hasab.runtime.core.HsString

HsString.length("hello")                    // 5
HsString.charAt("hello", 1)                // 'e'
HsString.substring("hello world", 0, 5)    // "hello"
HsString.toUpperCase("hello")              // "HELLO"
HsString.toLowerCase("HELLO")             // "hello"
HsString.trim("  hello  ")                // "hello"
HsString.startsWith("hello", "he")        // true
HsString.endsWith("hello", "lo")          // true
HsString.contains("hello world", "world") // true
HsString.indexOf("hello", "ll")           // 2
HsString.replace("hello", "l", "r")       // "herro"
HsString.split("a,b,c", ",")             // ["a", "b", "c"]
HsString.join(arrayOf("a", "b"), "-")     // "a-b"
HsString.repeat("ha", 3)                  // "hahaha"
HsString.reverse("hello")                 // "olleh"
HsString.format("Hello, %s!", "World")    // "Hello, World!"
HsString.isAlpha("hello")                 // true
HsString.isNumeric("12345")               // true
HsString.isAlphaNumeric("abc123")          // true
HsString.encode("hello")                   // ByteArray (UTF-8)
HsString.decode(byteArrayOf(0x68, 0x69))   // "hi"
```

#### HsInt

Integer utilities including parsing, conversion, and digit operations.

```kotlin
import hasab.runtime.core.HsInt

HsInt.MAX_VALUE                    // 2147483647
HsInt.MIN_VALUE                    // -2147483648
HsInt.parseInt("42")               // 42
HsInt.parseIntOrNull("abc")        // null
HsInt.toString(42)                 // "42"
HsInt.toFloat(42)                  // 42.0f
HsInt.toByteArray(256)             // [0, 0, 1, 0]
HsInt.fromByteArray(byteArrayOf(0, 0, 1, 0))  // 256
HsInt.digits(12345)               // [1, 2, 3, 4, 5]
HsInt.isEven(4)                    // true
HsInt.isOdd(3)                     // true
HsInt.coerceIn(15, 0, 10)         // 10
HsInt.clamp(15, 0, 10)            // 10
HsInt.compare(1, 2)               // -1
```

#### HsFloat

Floating-point utilities.

```kotlin
import hasab.runtime.core.HsFloat

HsFloat.MAX_VALUE                     // 3.4028235E38
HsFloat.NaN                           // NaN
HsFloat.POSITIVE_INFINITY             // Infinity
HsFloat.isNaN(HsFloat.NaN)           // true
HsFloat.isInfinite(HsFloat.POSITIVE_INFINITY)  // true
HsFloat.isFinite(3.14f)             // true
HsFloat.parseInt("3.14")             // 3 (truncated)
HsFloat.parseFloat("3.14")           // 3.14f
HsFloat.coerceIn(15f, 0f, 10f)     // 10f
HsFloat.compare(1.0f, 2.0f)        // -1
```

#### HsChar

Character classification and conversion.

```kotlin
import hasab.runtime.core.HsChar

HsChar.isLetter('A')          // true
HsChar.isDigit('5')           // true
HsChar.isLetterOrDigit('a')   // true
HsChar.isUpperCase('A')       // true
HsChar.isLowerCase('a')       // true
HsChar.toUpperCase('a')       // 'A'
HsChar.toLowerCase('A')       // 'a'
HsChar.isWhitespace(' ')      // true
HsChar.isControl('\u0003')    // true
HsChar.toInt('A')             // 65
HsChar.fromInt(65)            // 'A'
HsChar.compare('a', 'b')     // -1
```

#### HsBool

Boolean logic utilities.

```kotlin
import hasab.runtime.core.HsBool

HsBool.and(true, false)       // false
HsBool.or(true, false)        // true
HsBool.not(true)              // false
HsBool.xor(true, false)       // true
HsBool.toString(true)         // "true"
HsBool.parseBoolean("yes")    // false (only "true" is true)
HsBool.countTrue(true, false, true)  // 2
HsBool.countFalse(true, false, true) // 1
```

#### HsArray

Array creation, access, and transformation utilities. Operates on `Array<Any?>`.

```kotlin
import hasab.runtime.core.HsArray

val arr = HsArray.create(5) { it * 2 }   // [0, 2, 4, 6, 8]
HsArray.get(arr, 2)                       // 4
HsArray.set(arr, 2, 99)                   // arr[2] = 99
HsArray.size(arr)                         // 5
HsArray.contains(arr, 6)                  // true
HsArray.sort(HsArray.create(3) { listOf(3, 1, 2)[it] }) // [1, 2, 3]

val mapped = HsArray.map(arr) { (it as Int) * 10 }
val filtered = HsArray.filter(arr) { (it as Int) > 4 }
val flattened = HsArray.flatten(arrayOf(arrayOf(1, 2), arrayOf(3, 4)))
val distinct = HsArray.distinct(arrayOf(1, 2, 2, 3))

// Typed primitive arrays
val ints = HsArray.createIntArray(10)         // IntArray(10) all zeros
val floats = HsArray.createFloatArray(5)      // FloatArray(5) all zeros
val bools = HsArray.createBoolArray(5)        // BooleanArray(5) all false
```

---

### 3.2 collections/ — Collection Types

#### HsList (Immutable)

Wraps a read-only `List<Any?>`. All transformation methods return new `HsList` instances.

```kotlin
import hasab.runtime.collections.HsList

val list = HsList.of(3, 1, 4, 1, 5, 9)
list.size()                          // 6
list.get(0)                          // 3
list.contains(5)                     // true
list.indexOf(1)                      // 1
list.first()                         // 3
list.last()                          // 9

// Transformations
list.sorted()                        // HsList(1, 1, 3, 4, 5, 9)
list.reversed()                      // HsList(9, 5, 1, 4, 1, 3)
list.distinct()                      // HsList(3, 1, 4, 5, 9)
list.take(3)                         // HsList(3, 1, 4)
list.drop(2)                         // HsList(4, 1, 5, 9)
list.filter { (it as Int) > 3 }     // HsList(4, 5, 9)
list.map { (it as Int) * 2 }        // HsList(6, 2, 8, 2, 10, 18)
list.reduce { a, b -> (a as Int) + (b as Int) }  // 23
list.fold(0) { acc, v -> (acc as Int) + (v as Int) } // 23

// Combining
val other = HsList.of(10, 20)
list.plus(other)                     // HsList(3, 1, 4, 1, 5, 9, 10, 20)
list.minus(1)                        // HsList(3, 4, 5, 9) (removes first occurrence)
list.zip(HsList.of("a", "b", "c")) // HsList((3,a), (1,b), (4,c))

// Conversion
list.toMutableList()                 // HsMutableList
list.toSet()                         // HsSet
list.toArray()                       // Array<Any?>
list.joinToString(", ")             // "3, 1, 4, 1, 5, 9"
```

#### HsMutableList

Extends HsList with mutation operations.

```kotlin
import hasab.runtime.collections.HsMutableList

val list = HsMutableList.of(1, 2, 3)
list.add(4)                          // list is now [1, 2, 3, 4]
list.add(0, 0)                      // list is now [0, 1, 2, 3, 4]
list.set(2, 99)                     // returns 2, list is [0, 1, 99, 3, 4]
list.remove(99)                     // list is [0, 1, 3, 4]
list.removeAt(0)                    // returns 0, list is [1, 3, 4]
list.addAll(listOf(5, 6))          // list is [1, 3, 4, 5, 6]
list.sort(compareBy { it as Int }) // in-place sort
list.reverse()                      // in-place reverse
list.shuffle()                      // in-place shuffle
list.clear()                        // list is empty

val fresh = HsMutableList.create(100) // pre-allocated capacity
```

#### HsMap (Immutable)

Wraps a read-only `Map<Any?, Any?>`.

```kotlin
import hasab.runtime.collections.HsMap

val map = HsMap.of("a" to 1, "b" to 2, "c" to 3)
map.size()                           // 3
map.get("b")                         // 2
map.getOrDefault("z", 0)            // 0
map.containsKey("a")                // true
map.containsValue(3)                // true
map.keys()                           // HsSet(a, b, c)
map.values()                         // HsList(1, 2, 3)

map.filter { k, v -> (v as Int) > 1 }   // HsMap(b=2, c=3)
map.mapValues { _, v -> (v as Int) * 10 } // HsMap(a=10, b=20, c=30)
map.filterKeys { it == "a" || it == "c" } // HsMap(a=1, c=3)

val merged = map.plus(HsMap.of("d" to 4))
val reduced = map.minus("b")

map.forEach { k, v -> HsIO.println("$k=$v") }
```

#### HsMutableMap

Extends HsMap with mutation operations.

```kotlin
import hasab.runtime.collections.HsMutableMap

val map = HsMutableMap.of("a" to 1)
map.put("b", 2)                     // returns null
map.put("a", 10)                    // returns 1 (previous value)
map.remove("b")                     // returns 2
map.putAll(mapOf("x" to 99, "y" to 100))
map.getOrPut("z") { 0 }            // inserts 0, returns 0
map.computeIfAbsent("w") { (it as String).length }  // inserts 1
map.clear()
```

#### HsSet (Immutable)

```kotlin
import hasab.runtime.collections.HsSet

val set = HsSet.of(1, 2, 3, 4, 5)
set.contains(3)                     // true
set.size()                          // 5

val evens = set.filter { (it as Int) % 2 == 0 }   // HsSet(2, 4)
val mapped = set.map { (it as Int) * 2 }           // HsSet(2, 4, 6, 8, 10)

val a = HsSet.of(1, 2, 3)
val b = HsSet.of(3, 4, 5)
a.union(b)                          // HsSet(1, 2, 3, 4, 5)
a.intersect(b)                      // HsSet(3)
a.minus(b)                          // HsSet(1, 2)
```

#### HsMutableSet

```kotlin
import hasab.runtime.collections.HsMutableSet

val set = HsMutableSet.of(1, 2, 3)
set.add(4)                          // true (was not present)
set.add(1)                          // false (already present)
set.remove(2)                       // true
set.addAll(listOf(5, 6, 7))       // true
set.removeAll(setOf(5, 6))         // true
set.retainAll(setOf(1, 3, 8))     // true (only 1, 3 remain)
set.clear()
```

#### HsStack (LIFO)

```kotlin
import hasab.runtime.collections.HsStack

val stack = HsStack.of(1, 2, 3)  // bottom: 1, top: 3
stack.push(4)
stack.peek()                        // 4 (does not remove)
stack.pop()                         // 4
stack.pop()                         // 3
stack.search(1)                     // 2 (distance from top)
stack.contains(2)                   // true
stack.isEmpty()                     // false
stack.toList()                      // HsList(2, 1) (top first)
```

#### HsQueue (FIFO)

```kotlin
import hasab.runtime.collections.HsQueue

val queue = HsQueue.of("a", "b", "c")
queue.peek()                        // "a" (does not remove)
queue.poll()                        // "a" (removes and returns)
queue.poll()                        // "b"
queue.offer("d")                    // true
queue.add("e")                      // true
queue.remove()                      // "c" (throws if empty)
queue.contains("d")                 // true
queue.toList()                      // HsList(d, e) (front to back)
```

---

### 3.3 io/ — I/O Operations

#### HsIO

Console I/O and stream operations.

```kotlin
import hasab.runtime.io.HsIO

// Console output
HsIO.print("Enter name: ")
HsIO.println("Hello, World!")
HsIO.println()                      // blank line

// Console input
val name: String? = HsIO.readLine()
val password: CharArray? = HsIO.readPassword()
val input: String = HsIO.readInput("Prompt: ")

// Stream operations
val bytes: ByteArray = HsIO.readAllBytes(inputStream)
val text: String = HsIO.readAllText(inputStream, "UTF-8")
HsIO.writeAllBytes(outputStream, byteArray)
HsIO.writeAllText(outputStream, "text content", "UTF-8")
val transferred: Long = HsIO.copyStream(inputStream, outputStream)

// Classpath resources
val resource: String? = HsIO.readResource("/config.properties")
val resourceBytes: ByteArray? = HsIO.readResourceBytes("/data.bin")
val resourceStream: InputStream? = HsIO.openResource("/template.txt")
```

#### HsFile

File wrapper with comprehensive file system operations.

```kotlin
import hasab.runtime.io.HsFile

val file = HsFile("output.txt")
file.path                           // "output.txt"
file.name                           // "output.txt"
file.extension                      // "txt"
file.absolutePath                   // "D:\projects\hasab-lang\output.txt"
file.exists                         // false (before creation)
file.isFile                         // true
file.isDirectory                    // false
file.size                           // 0L
file.isReadable                     // true/false
file.isWritable                     // true/false

// Read/write
file.writeAllText("Hello, World!")
val content: String = file.readAllText()
val lines: List<String> = file.readAllLines()
val bytes: ByteArray = file.readAllBytes()

// Append
file.appendText("More content")
file.appendLine("A line")

// Manipulation
file.createNewFile()                // creates empty file
file.copyTo(HsFile("backup.txt"))  // true
file.moveTo(HsFile("renamed.txt")) // true
file.delete()                       // true

// Directory operations
val dir = HsFile("mydir")
dir.mkdirs()
val children: Array<HsFile> = dir.listFiles()
val ktFiles: Array<HsFile> = dir.listFilesFiltered("kt")

// Line iteration
file.forEachLine { line -> HsIO.println(line) }

// Temp files
val temp = HsFile.tempFile("prefix", ".txt")   // auto-deleted on exit
val tempDir = HsFile.createTempDirectory("work")

// Relative path resolution
val resolved = dir.resolve("subdir/file.txt")
val relative = file.relativeTo(dir)
```

---

### 3.4 text/ — Text Processing

#### HsText

Comprehensive string processing with `ignoreCase` support, regex, and more.

```kotlin
import hasab.runtime.text.HsText

// Basic operations
HsText.length("hello")                  // 5
HsText.isEmpty("")                       // true
HsText.isBlank("  ")                    // true
HsText.upper("hello")                   // "HELLO"
HsText.lower("HELLO")                  // "hello"
HsText.capitalize("hello")             // "Hello"
HsText.decapitalize("Hello")           // "hello"

// Trimming & padding
HsText.trim("  hello  ")               // "hello"
HsText.trimStart("  hello")            // "hello  "
HsText.trimEnd("hello  ")             // "  hello"
HsText.padStart("42", 5, '0')         // "00042"
HsText.padEnd("hi", 5, '.')            // "hi..."
HsText.center("hi", 6, '-')            // "--hi--"

// Searching
HsText.contains("Hello World", "World")            // true
HsText.contains("Hello", "hello", ignoreCase = true) // true
HsText.startsWith("Hello", "Hel")                   // true
HsText.endsWith("Hello", "llo")                     // true
HsText.indexOf("Hello World", "World")              // 6
HsText.lastIndexOf("abcabc", "abc")                 // 3

// Replacing
HsText.replace("hello world", "world", "there")     // "hello there"
HsText.replaceFirst("aabaa", "a", "X")              // "Xabaa"
HsText.replaceRegex("abc123", "\\d+", "#")           // "abc#"
HsText.findAll("\\d+", "abc123def456")              // ["123", "456"]

// Splitting & joining
HsText.split("a,b,c", ",")                          // ["a", "b", "c"]
HsText.split("a,,b,,c", ",", limit = 2)            // ["a", ",,b,,c"]
HsText.join(listOf(1, 2, 3), " - ")                 // "1 - 2 - 3"

// Substrings
HsText.substring("Hello World", 6)                   // "World"
HsText.take("Hello", 3)                             // "Hel"
HsText.takeLast("Hello", 3)                         // "llo"
HsText.drop("Hello", 2)                             // "llo"
HsText.dropLast("Hello", 2)                         // "Hel"
HsText.chunked("abcdefgh", 3)                       // ["abc", "def", "gh"]

// Regex
HsText.matches("hello123", "^hello\\d+$")           // true

// Classification
HsText.isAlpha("abc")                               // true
HsText.isNumeric("123")                             // true
HsText.isAlphaNumeric("abc123")                     // true
HsText.isUpperCase("HELLO")                         // true
HsText.isLowerCase("hello")                         // true

// String analysis
HsText.commonPrefix("hello", "help")                // "hel"
HsText.commonSuffix("testing", "ing")               // "ing"
HsText.truncate("Hello World", 8)                   // "Hello..."
HsText.levenshteinDistance("kitten", "sitting")     // 3

// HTML
HsText.escapeHtml("<script>alert('xss')</script>")  // "&lt;script&gt;..."
HsText.unescapeHtml("&lt;div&gt;")                  // "<div>"

// Formatting
HsText.indent("line1\nline2", 4)                    // "    line1\n    line2"
HsText.reverse("hello")                             // "olleh"
HsText.repeat("ha", 3)                              // "hahaha"
```

---

### 3.5 math/ — Mathematics

#### HsMath

Math constants and operations for both `Int` and `Double`.

```kotlin
import hasab.runtime.math.HsMath

// Constants
HsMath.PI                            // 3.141592653589793
HsMath.E                             // 2.718281828459045
HsMath.SQRT2                         // 1.4142135623730951
HsMath.LN2                           // 0.6931471805599453

// Integer operations
HsMath.abs(-5)                       // 5
HsMath.max(3, 7)                     // 7
HsMath.min(3, 7)                     // 3
HsMath.pow(2, 10)                    // 1024
HsMath.sqrt(16)                      // 4
HsMath.gcd(12, 8)                    // 4
HsMath.lcm(4, 6)                     // 12
HsMath.clamp(15, 0, 10)             // 10
HsMath.floorDiv(-7, 2)              // -4 (not -3)
HsMath.floorMod(-7, 2)              // 1  (not -1)

// Float operations
HsMath.abs(-3.14)                    // 3.14
HsMath.pow(2.0, 0.5)                // 1.4142135623730951
HsMath.sqrt(2.0)                     // 1.4142135623730951
HsMath.cbrt(27.0)                    // 3.0
HsMath.clamp(1.5, 0.0, 1.0)        // 1.0

// Trigonometry
HsMath.sin(HsMath.PI / 2)           // 1.0
HsMath.cos(0.0)                      // 1.0
HsMath.tan(HsMath.PI / 4)           // ~1.0
HsMath.asin(1.0)                     // ~PI/2
HsMath.atan2(1.0, 0.0)             // ~PI/2
HsMath.toDegrees(HsMath.PI)         // 180.0
HsMath.toRadians(180.0)             // ~PI
HsMath.hypot(3.0, 4.0)             // 5.0

// Hyperbolic
HsMath.sinh(1.0)                     // 1.1752011936438014
HsMath.cosh(1.0)                     // 1.5430806348152437

// Logarithmic & exponential
HsMath.log(HsMath.E)                // 1.0
HsMath.log2(8.0)                     // 3.0
HsMath.log10(1000.0)                // 3.0
HsMath.exp(1.0)                      // 2.718281828459045
HsMath.exp2(10.0)                    // 1024.0

// Rounding
HsMath.ceil(3.2)                     // 4.0
HsMath.floor(3.8)                    // 3.0
HsMath.round(3.5)                    // 4L
HsMath.sign(-5.0)                    // -1.0

// Random
HsMath.random()                      // 0.0 to 1.0
HsMath.randomInt(1, 10)             // 1 to 10 inclusive
HsMath.randomDouble(0.0, 1.0)       // 0.0 to 1.0
```

---

### 3.6 datetime/ — Date & Time

#### HsDateTime

Date/time operations using ISO 8601 format strings.

```kotlin
import hasab.runtime.datetime.HsDateTime

// Current time
HsDateTime.now()                     // "2026-07-19T14:30:00.000Z"
HsDateTime.today()                   // "2026-07-19"
HsDateTime.currentTimeMillis()      // 1752945000000L

// Parsing
val millis: Long = HsDateTime.parseDateTime("2026-07-19T14:30:00Z")
val dateStr: String = HsDateTime.parseDate("2026-07-19")
val timeStr: String = HsDateTime.parseTime("14:30:00")

// Formatting
HsDateTime.formatDateTime(millis, "yyyy-MM-dd HH:mm")  // "2026-07-19 14:30"
HsDateTime.formatDate(millis, "dd/MM/yyyy")              // "19/07/2026"
HsDateTime.formatTime(millis, "HH:mm:ss")               // "14:30:00"

// Components
val dt = "2026-07-19T14:30:45Z"
HsDateTime.year(dt)                  // 2026
HsDateTime.month(dt)                 // 7
HsDateTime.day(dt)                   // 19
HsDateTime.hour(dt)                  // 14
HsDateTime.minute(dt)                // 30
HsDateTime.second(dt)                // 45
HsDateTime.dayOfWeek(dt)             // "SATURDAY"
HsDateTime.dayOfYear(dt)             // 200

// Date arithmetic
HsDateTime.addDays(dt, 30)           // "2026-08-18T14:30:45Z"
HsDateTime.addHours(dt, 2)           // "2026-07-19T16:30:45Z"
HsDateTime.addMinutes(dt, -15)       // "2026-07-19T14:15:45Z"
HsDateTime.addSeconds(dt, 3600)      // "2026-07-19T15:30:45Z"

// Diffing
val later = "2026-08-19T14:30:45Z"
HsDateTime.diffDays(dt, later)       // 31
HsDateTime.diffHours(dt, later)      // 744
HsDateTime.diffMinutes(dt, later)    // 44640

// Conversion
HsDateTime.toEpochMillis(dt)         // millis
HsDateTime.fromEpochMillis(millis)   // "2026-07-19T14:30:00Z"
HsDateTime.toUtc(dt)                 // UTC string
HsDateTime.toLocal(dt)               // local datetime string
HsDateTime.withTimeZone(dt, "America/New_York")  // converted string

// Calendar info
HsDateTime.isLeapYear(2026)          // false
HsDateTime.daysInMonth(2026, 2)      // 28
HsDateTime.daysInYear(2026)          // 365
HsDateTime.age("1990-07-19")         // 36
```

---

### 3.7 concurrency/ — Threading

#### HsThread

```kotlin
import hasab.runtime.concurrency.HsThread

// Create and start a thread
val thread = HsThread.of("worker", daemon = false) {
    HsIO.println("Running in thread: ${HsThread.currentThread().name}")
    HsThread.sleep(1000)
}
thread.start()
thread.join(5000)                    // wait up to 5 seconds
thread.isAlive                       // false if finished
thread.id                            // thread ID
thread.name                          // "worker"
thread.setPriority(5)

// Current thread
val current = HsThread.currentThread()
current.interrupt()

// Static utilities
HsThread.sleep(100)
HsThread.yield()
HsThread.activeCount()
```

#### HsLock & HsCondition

```kotlin
import hasab.runtime.concurrency.HsLock

val lock = HsLock()

// Basic locking
lock.lock()
try {
    // critical section
} finally {
    lock.unlock()
}

// Try lock
if (lock.tryLock()) { /* acquired */ }
if (lock.tryLock(5000)) { /* acquired within 5s */ }

lock.isLocked()
lock.isHeldByCurrentThread()

// Conditions
val condition = lock.newCondition()
lock.lock()
try {
    condition.await()                    // wait for signal
    condition.await(5000)                // wait with timeout
    condition.signal()                   // wake one waiter
    condition.signalAll()                // wake all waiters
} finally {
    lock.unlock()
}
```

#### HsFuture

```kotlin
import hasab.runtime.concurrency.HsFuture

// Async computation
val future = HsFuture.of {
    HsThread.sleep(1000)
    "result"
}

// Pre-completed futures
val immediate = HsFuture.completed(42)
val failed = HsFuture.failed(RuntimeException("oops"))

// Blocking get
val result = future.get()              // waits, returns "result"
val result2 = future.get(5000)         // waits up to 5s

// Completion state
future.isDone                          // true/false
future.isCancelled                     // false
future.cancel()                        // attempt cancellation

// Chaining
val transformed = future.thenApply { (it as String).uppercase() }
future.thenAccept { HsIO.println("Got: $it") }
future.exceptionally { "fallback" }
future.whenComplete { result, error -> /* cleanup */ }

// Combining
val f1 = HsFuture.completed(1)
val f2 = HsFuture.completed(2)
val all = HsFuture.allOf(f1, f2)       // completes with list of all results
val any = HsFuture.anyOf(f1, f2)       // completes with first result
```

#### HsExecutor

```kotlin
import hasab.runtime.concurrency.HsExecutor

// Create thread pools
val fixed = HsExecutor.fixedThreadPool(4)
val single = HsExecutor.singleThread()
val cached = HsExecutor.cachedThreadPool()
val virtual = HsExecutor.virtualThreads()  // Java 21 virtual threads

// Default constructor = fixed pool sized to available processors
val executor = HsExecutor()

// Submit tasks
val future = executor.submit { computeExpensiveValue() }
val futures = executor.submitAll(listOf(
    { task1() },
    { task2() },
    { task3() }
))

// Lifecycle
executor.isShutdown                   // false
executor.shutdown()                   // orderly shutdown
executor.isTerminated                 // true after all tasks finish
executor.awaitTermination(10000)      // wait up to 10s
executor.shutdownNow()                // force stop
```

---

### 3.8 filesystem/ — File System Operations

#### HsPath

Cross-platform path manipulation.

```kotlin
import hasab.runtime.filesystem.HsPath

HsPath.separator                       // "\" on Windows, "/" on Unix
HsPath.normalize("a/b/../c")          // "a\c"
HsPath.toAbsolute("relative/path")    // "D:\projects\hasab-lang\relative\path"
HsPath.toCanonical("a/b/../c")        // "D:\projects\hasab-lang\a\c"
HsPath.getFileName("/path/to/file.txt")  // "file.txt"
HsPath.getParent("/path/to/file.txt")    // "/path/to"
HsPath.getExtension("file.txt")          // "txt"
HsPath.getBaseName("file.txt")           // "file"
HsPath.isAbsolute("/path")              // true
HsPath.isRelative("path")              // true
HsPath.join("a", "b", "c")            // "a\b\c"
HsPath.resolve("/base", "relative")     // "/base/relative"
HsPath.relativize("/a/b", "/a/b/c/d")  // "c\d"
HsPath.changeExtension("file.txt", "md")  // "file.md"
HsPath.hasExtension("file.txt", "txt")   // true
HsPath.roots()                           // ["C:\", "D:\", ...]
HsPath.tempDir()                         // system temp dir
HsPath.homeDir()                         // user home dir
```

#### HsDirectory

Directory operations with recursive traversal.

```kotlin
import hasab.runtime.filesystem.HsDirectory

val dir = HsDirectory("src/main")
dir.exists                            // true
dir.isEmpty                           // false
dir.childCount                        // number of direct children
dir.size                              // total recursive file count

// Creation
dir.create()                          // creates with parents
val sub = dir.createSubdirectory("kotlin")
val file = dir.createFile("README.md")

// Listing
val allFiles: List<HsFile> = dir.listFiles()
val filesOnly: List<HsFile> = dir.listFilesOnly()
val dirsOnly: List<HsDirectory> = dir.listDirectories()

// Recursive traversal
val allFilesRecursive: List<HsFile> = dir.walkFiles()
val allDirsRecursive: List<HsDirectory> = dir.walkDirectories()

// Copy and relative paths
dir.copyTo(HsDirectory("backup"))
val relativePath = dir.relatize(file)

// Temp directory
val tempDir = HsDirectory.tempDir("build")  // auto-deleted on exit
```

#### HsFileWatcher

Polling-based file change watcher.

```kotlin
import hasab.runtime.filesystem.HsFileWatcher
import hasab.runtime.filesystem.HsWatchEvent

val watcher = HsFileWatcher("src/main", intervalMs = 500)

watcher.onEvent { event ->
    when (event.kind) {
        HsWatchEvent.Kind.CREATED -> HsIO.println("Created: ${event.path}")
        HsWatchEvent.Kind.MODIFIED -> HsIO.println("Modified: ${event.path}")
        HsWatchEvent.Kind.DELETED -> HsIO.println("Deleted: ${event.path}")
    }
}

watcher.start()
// ... later
watcher.stop()
watcher.isRunning                     // false
```

#### HsTempFile

Managed temporary files with automatic cleanup.

```kotlin
import hasab.runtime.filesystem.HsTempFile

val temp = HsTempFile.create("config", ".json")
temp.writeText("""{"key": "value"}""")
val content: String = temp.readText()
val bytes: ByteArray = temp.readBytes()

temp.path                             // absolute path
temp.exists                           // true
temp.size                             // bytes

temp.registerCleanup()               // JVM shutdown hook
temp.cleanup()                       // explicit delete
temp.close()                         // same as cleanup

// Convert to HsFile for more operations
val hsFile: HsFile = temp.toHsFile()

// Temp directory
val tempDir = HsTempFile.createDirectory("build")
tempDir.isDirectory                   // true
```

---

### 3.9 network/ — Networking

#### HsUrl

URL parsing and manipulation.

```kotlin
import hasab.runtime.network.HsUrl

val url = HsUrl.parse("https://user:pass@example.com:8080/path?q=1#section")
url.href                              // full URL string
url.protocol                          // "https"
url.host                              // "example.com"
url.port                              // 8080
url.path                              // "/path"
url.query                             // "q=1"
url.fragment                          // "section"
url.username                          // "user"
url.password                          // "pass"
url.effectivePort                     // 8080
url.isSecure                          // true
url.origin                            // "https://example.com:8080"

url.pathSegments()                    // ["path"]

// Modify
val withParams = url.withQuery(mapOf("q" to "1", "page" to "2"))
val withFrag = url.withFragment("new-section")
val resolved = url.resolve("/other")

// Validation
HsUrl.isValid("https://example.com") // true
HsUrl.isValid("not a url")          // false

// Build from components
val built = HsUrl.build(
    protocol = "https",
    host = "api.example.com",
    path = "/v1/users",
    query = "limit=10"
)
```

#### HsHttpClient & HsHttpRequest

```kotlin
import hasab.runtime.network.HsHttpClient
import hasab.runtime.network.HsHttpRequest

// Create client
val client = HsHttpClient(
    defaultTimeoutMs = 30_000,
    followRedirects = true
)

// GET
val response = client.get("https://api.example.com/users")
if (response.isSuccessful) {
    val json: String = response.bodyAsString()
    val contentType: String = response.contentType
    val length: Long = response.contentLength
}

// POST with JSON body
val postResponse = client.post(
    "https://api.example.com/users",
    """{"name": "Alice"}""",
    mapOf("Authorization" to "Bearer token123")
)

// Request builder
val request = HsHttpRequest.Builder()
    .method("PUT")
    .url("https://api.example.com/users/1")
    .header("Authorization", "Bearer token123")
    .header("Content-Type", "application/json")
    .body("""{"name": "Bob"}""")
    .timeout(10_000)
    .build()

val putResponse = client.execute(request)

// Factory methods
val getReq = HsHttpRequest.get("https://example.com")
val postReq = HsHttpRequest.post("https://example.com", """{"data": 1}""")
val deleteReq = HsHttpRequest.delete("https://example.com/1")
val patchReq = HsHttpRequest.patch("https://example.com/1", """{"x": 1}""")
val headReq = HsHttpRequest.head("https://example.com")
```

#### HsHttpResponse

```kotlin
import hasab.runtime.network.HsHttpResponse

// Access response properties
response.statusCode                   // 200
response.statusMessage               // "OK"
response.isSuccessful                 // true (2xx)
response.isRedirect                   // false (3xx)
response.isClientError                // false (4xx)
response.isServerError                // false (5xx)
response.bodyAsString()              // string body
response.header("Content-Type")      // first value or null
response.contentType                  // content-type shorthand
response.contentLength                // content-length or body.size
response.url                          // final URL after redirects
response.headers                      // Map<String, List<String>>
```

---

### 3.10 exceptions/ — Error Types

All HASAB exceptions extend `HsException`, which extends `RuntimeException`.

```kotlin
import hasab.runtime.exceptions.*

// Exception hierarchy
//   HsException (base)
//     HsRuntimeException        — general runtime error
//     HsTypeException           — type mismatch
//     HsValueError              — invalid value argument
//     HsIOError                 — I/O failure
//     HsIndexOutOfBoundsException — array/list index out of range
//     HsNullPointerException    — null dereference
//     HsNotImplementedError     — unimplemented feature
//     HsStackOverflowError      — recursion too deep
//     HsDivisionByZeroError     — division by zero

// Catching
try {
    doSomethingRisky()
} catch (e: HsIOError) {
    HsIO.println("I/O failed: ${e.hsMessage}")
} catch (e: HsTypeException) {
    HsIO.println("Type error: ${e.hsMessage}")
} catch (e: HsException) {
    HsIO.println("Runtime error: ${e.hsMessage}")
}

// Throwing with factory methods
throw HsTypeException.create("String", "Int")  // "Type mismatch: expected String, got Int"
throw HsIndexOutOfBoundsException.create(10, 5) // "Index 10 is out of bounds for size 5"

// Creating directly
throw HsValueError("Value must be positive")
throw HsIOError("File not found", cause)
throw HsNullPointerException("Cannot access null field")
throw HsNotImplementedError("Async streams")
throw HsStackOverflowError()
throw HsDivisionByZeroError()
throw HsRuntimeException("Something went wrong")
```

---

### 3.11 reflection/ — Runtime Reflection

#### HsReflection

Dynamic type inspection, field access, and method invocation.

```kotlin
import hasab.runtime.reflection.HsReflection

// Type information
val obj = "Hello, World!"
HsReflection.className(obj)              // "java.lang.String"
HsReflection.simpleClassName(obj)        // "String"
HsReflection.classOf(obj)                // String::class.java

// Type checks
HsReflection.isInstance(obj, String::class.java)  // true
HsReflection.isAssignable(String::class.java, CharSequence::class.java)  // true
HsReflection.isPrimitive(42)             // false
HsReflection.isArray(intArrayOf())       // true
HsReflection.isEnum(MyEnum::class.java)  // true

// Field access
data class User(val name: String, var age: Int)
val user = User("Alice", 30)
HsReflection.getField(user, "name")     // "Alice"
HsReflection.setField(user, "age", 31)

// Static field access
HsReflection.getStaticField(System::class.java, "out")  // System.out
HsReflection.setStaticField(MyClass::class.java, "staticField", "new value")

// Method invocation
HsReflection.invokeMethod(user, "toString")  // "User(name=Alice, age=31)"
HsReflection.invokeStaticMethod(
    java.lang.Integer::class.java,
    "parseInt",
    "42"
)  // 42

// Constructors
val newUser = HsReflection.newInstance(User::class.java, "Bob", 25)

// Annotations
val annotations: Map<String, Any?> = HsReflection.getAnnotations(user)
val hasDeprecated = HsReflection.hasAnnotation(MyClass::class.java, Deprecated::class.java)

// Introspection
val methods: List<String> = HsReflection.methods(user)
val fields: Map<String, Any?> = HsReflection.fields(user)
val constructors: List<String> = HsReflection.constructors(User::class.java)
val interfaces: List<Class<*>> = HsReflection.interfaces(user)
val superclass: Class<*>? = HsReflection.superclass(user)

// Enum support
val values: Array<Any?> = HsReflection.getEnumValues(MyEnum::class.java)
val name: String = HsReflection.getEnumName(MyEnum.VALUE)
```

---

### 3.12 annotations/ — Meta-Annotations

HASAB annotations for documenting and constraining code behavior.

```kotlin
import hasab.runtime.annotations.*

// Deprecation
@HsDeprecated("Use newApi() instead", replaceWith = "newApi()")
fun oldApi() {}

// Access control
@HsInternalApi fun internalHelper() {}
@HsExperimentalApi fun unstableFeature() {}

// Thread safety
@HsThreadSafe class SharedState
@HsNotThreadSafe class NotSafeState

// Function properties
@HsPureFunction fun pure(a: Int, b: Int): Int = a + b
@HsSideEffect fun sideEffectful() { /* modifies state */ }

// Nullability
@HsNullable fun mayBeNull(): String? = null
@HsNonNull fun neverNull(): String = "always"

// Validation
@HsRange(min = 0, max = 100) val score: Int = 50
@HsPattern(regex = "\\d{4}-\\d{2}-\\d{2}") val date: String = "2026-07-19"
@HsMaxLength(max = 255) val name: String = "Alice"
@HsMinLength(min = 1) val required: String = "value"

// Metadata
@HsAuthor(name = "HASAB Team", email = "team@hasab.dev")
@HsSince(version = "1.0.0")
class MyLibrary
```

---

### 3.13 services/ — Runtime Services

#### HsVersion

```kotlin
import hasab.runtime.services.HsVersion

HsVersion.version                      // "1.0.0"
HsVersion.name                        // "HASAB Runtime"
HsVersion.buildDate                   // current date
HsVersion.javaVersion                 // JVM version
HsVersion.kotlinVersion               // Kotlin version
HsVersion.majorVersion()              // 1
HsVersion.minorVersion()              // 0
HsVersion.patchVersion()              // 0
HsVersion.isCompatibleWith("1.0.0")   // true
HsVersion.isCompatibleWith("2.0.0")   // false
HsVersion.info()                      // multi-line info string
HsVersion.toString()                  // "HASAB Runtime v1.0.0"
```

#### HsProfiler

```kotlin
import hasab.runtime.services.HsProfiler

// Manual timing
val timer = HsProfiler.timer("database")
// ... do work ...
timer.stop()
timer.elapsedMillis()                  // 42.5 ms
timer.isRunning()                     // false
timer.reset()

// Measure block
val nanos = HsProfiler.measure("parse") {
    parseLargeInput()
}

// Report
HsProfiler.report()
// === HASAB Profiler Report ===
//   parse                             123.456 ms
//   database                           42.500 ms
// === End Report ===

HsProfiler.resetAll()
HsProfiler.getTimers()               // Map<String, Timer>
```

---

### 3.14 util/ — Utilities

#### HsPlatform

```kotlin
import hasab.runtime.util.HsPlatform

HsPlatform.osName                      // "Windows 10"
HsPlatform.osArch                      // "amd64"
HsPlatform.javaVersion                 // "21.0.1"
HsPlatform.kotlinVersion               // "2.0.21"
HsPlatform.userHome                    // "C:\Users\user"
HsPlatform.userDir                     // "D:\projects\hasab-lang"
HsPlatform.tempDir                     // "C:\Users\user\AppData\Local\Temp"
HsPlatform.lineSeparator               // "\r\n" or "\n"
HsPlatform.availableProcessors         // 8
HsPlatform.maxMemory                   // JVM max memory bytes
HsPlatform.totalMemory                 // JVM total memory bytes
HsPlatform.freeMemory                  // JVM free memory bytes

HsPlatform.isWindows()                 // true/false
HsPlatform.isMacOS()                   // true/false
HsPlatform.isLinux()                   // true/false
HsPlatform.isUnix()                    // true/false
HsPlatform.is64Bit()                   // true/false

HsPlatform.getEnvironment("HOME")      // env var value or null
HsPlatform.getSystemProperty("user.name")  // sys prop value or null
HsPlatform.nanoTime()                  // high-res time
HsPlatform.currentTimeMillis()        // wall-clock time
HsPlatform.uptimeMillis()             // JVM uptime
HsPlatform.gc()                        // hint to GC
HsPlatform.exit(0)                     // terminate JVM
```

#### HsLog

```kotlin
import hasab.runtime.util.HsLog

// Configure
HsLog.currentLevel = HsLog.LogLevel.DEBUG
HsLog.output = { message -> myLogger.write(message) }

// Log at levels
HsLog.debug("Processing item {}", itemId)
HsLog.info("Server started on port {}", 8080)
HsLog.warn("Disk usage at {}%", 95)
HsLog.error("Connection failed: {}", error.message)
HsLog.error("Critical failure", exception)
HsLog.fatal("Unrecoverable error: {}", reason)

// Level checks (guard expensive string construction)
if (HsLog.isDebugEnabled()) {
    HsLog.debug("Expensive: {}", computeDiagnostics())
}

HsLog.isDebugEnabled()                // true if level <= DEBUG
HsLog.isInfoEnabled()                 // true if level <= INFO
HsLog.isWarnEnabled()                 // true if level <= WARNING
HsLog.isErrorEnabled()                // true if level <= ERROR

// Output format:
// 2026-07-19T14:30:00.123Z [INFO   ] Server started on port 8080
```

#### HsConfig

```kotlin
import hasab.runtime.util.HsConfig

// Create
val config = HsConfig()
val configWithDefaults = HsConfig(mapOf("port" to 8080, "debug" to true))

// Load from sources
val fromFile = HsConfig.loadFromFile("app.properties")
val fromResource = HsConfig.loadFromResource("defaults.properties")
val fromMap = HsConfig.loadFromProperties(mapOf("key" to "value"))

// Get with type coercion
config.getString("name", "default")
config.getInt("port", 8080)
config.getLong("id", 0L)
config.getBoolean("debug", false)
config.getDouble("ratio", 1.0)

// Set and manage
config.set("key", "value")
config.has("key")                     // true
config.remove("key")
config.keys()                         // Set<String>
config.values()                       // Map<String, Any?>
config.size()
config.isEmpty()

// Merge and clone
config.merge(otherConfig)
val copy = config.clone()
```

#### HsLocalize

```kotlin
import hasab.runtime.util.HsLocalize

// Static convenience
HsLocalize.translate("messages.greeting")     // translated string or raw key
HsLocalize.translate("messages.hello", "Alice") // with MessageFormat args

// Instance-based
val localize = HsLocalize("fr")
localize.setLocale("de")
localize.defaultLocale = "en"

localize.translate("app.title")
localize.hasKey("app.title")            // true/false
localize.getAvailableLocales()          // ["fr", "de"]
localize.loadBundle("messages", "es")
localize.getBundleKeys("messages")      // set of keys

// Thread locale singleton
val global = HsLocalize.instance()
global.translate("shared.key")
```

---

### 3.15 internal/ — Internal Helpers

`HsInternal` provides `@PublishedApi internal` helpers used by compiler-generated bytecode. **Do not call these directly from user code.**

```kotlin
// These are invoked by the HASAB compiler, not by application code:
// HsInternal.checkNotNull(value)
// HsInternal.checkType(value, "Int")
// HsInternal.checkIndex(index, size)
// HsInternal.divisionByZero()
// HsInternal.nullReference()
// HsInternal.notImplemented("feature")
// HsInternal.valueError("message")
// HsInternal.runtimeError("message")
// HsInternal.ioError("message", cause)
// HsInternal.formatStacktrace(throwable)
// HsInternal.safeToString(obj)
```

---

## 4. Configuration & Setup

### Build Requirements

- JDK 21 or later
- Kotlin 2.0.21
- Gradle 8.x+

### Gradle Dependency

```kotlin
dependencies {
    implementation("hasab.lang:hasab-runtime:1.0.0")
}
```

### Verifying the Build

```bash
./gradlew build        # compile + test
./gradlew test         # test only
./gradlew lint         # check code style
```

### Module Import Pattern

Import the specific class you need. All public APIs live in `hasab.runtime.*`:

```kotlin
import hasab.runtime.core.HsString
import hasab.runtime.collections.HsList
import hasab.runtime.io.HsIO
import hasab.runtime.math.HsMath
import hasab.runtime.datetime.HsDateTime
```

---

## 5. Error Handling

### Exception Hierarchy

All HASAB exceptions inherit from `HsException extends RuntimeException`:

```
RuntimeException
  └── HsException
        ├── HsRuntimeException
        ├── HsTypeException
        ├── HsValueError
        ├── HsIOError
        ├── HsIndexOutOfBoundsException
        ├── HsNullPointerException
        ├── HsNotImplementedError
        ├── HsStackOverflowError
        └── HsDivisionByZeroError
```

### Patterns

**Pattern 1: Catch-all with `hsMessage`**

```kotlin
try {
    riskyOperation()
} catch (e: HsException) {
    HsIO.println("Error [${e::class.simpleName}]: ${e.hsMessage}")
}
```

**Pattern 2: Specific exception handling**

```kotlin
try {
    val list = HsList.of(1, 2, 3)
    val value = list.get(10)
} catch (e: HsIndexOutOfBoundsException) {
    HsLog.error("Index error: {}", e.hsMessage)
} catch (e: HsNullPointerException) {
    HsLog.error("Null reference: {}", e.hsMessage)
}
```

**Pattern 3: Throw specific exceptions with factory methods**

```kotlin
fun getAge(birthYear: Int): Int {
    if (birthYear < 0) throw HsValueError("birthYear must be non-negative, got $birthYear")
    val currentYear = HsDateTime.year(HsDateTime.now())
    return currentYear - birthYear
}
```

**Pattern 4: Resource cleanup**

```kotlin
import hasab.runtime.filesystem.HsTempFile

HsTempFile.create().use { temp ->
    temp.writeText("data")
    processFile(temp.toHsFile())
}  // auto-cleaned
```

---

## 6. Best Practices

### Prefer Immutable Collections

Use `HsList` over `HsMutableList` unless you need mutation. Immutable collections are safer and easier to reason about.

```kotlin
// Good
val list = HsList.of(1, 2, 3)
val filtered = list.filter { (it as Int) > 1 }

// Avoid unless mutation is needed
val mutable = HsMutableList.of(1, 2, 3)
```

### Use Type-Safe Wrappers

Always wrap primitive types in the corresponding `Hs*` wrapper when calling HASAB APIs.

### Profile Before Optimizing

Use `HsProfiler.measure()` to identify hotspots before optimizing:

```kotlin
val nanos = HsProfiler.measure("data-processing") {
    processLargeDataSet()
}
HsIO.println("Took ${nanos / 1_000_000.0} ms")
```

### Use `HsConfig` for Application Settings

```kotlin
val config = HsConfig.loadFromFile("config.properties")
val port = config.getInt("server.port", 8080)
val debug = config.getBoolean("app.debug", false)
```

### Log at the Right Level

```kotlin
HsLog.debug("Variable state: {}", dumpState())    // development only
HsLog.info("Server started on port {}", port)      // normal operations
HsLog.warn("Cache miss rate: {}%", missRate)       // degraded but functional
HsLog.error("Request failed", exception)           // failure requiring attention
HsLog.fatal("Database unreachable")               // system cannot continue
```

---

## 7. Performance Tips

### Batch Operations

Use `submitAll()` on executors instead of submitting tasks one-by-one:

```kotlin
val executor = HsExecutor.fixedThreadPool(8)
val futures = executor.submitAll(tasks)
futures.forEach { it.get() }
executor.shutdown()
```

### Avoid Excessive Collection Copies

Chained transformations create intermediate collections. Consider using `fold()` or `reduce()` for complex pipelines.

### Use `HsMutableList` for Build Phases

When constructing large collections incrementally, use the mutable variant then convert:

```kotlin
val mutable = HsMutableList.create(expectedSize)
repeat(expectedSize) { mutable.add(computeItem(it)) }
val immutable: HsList = mutable.toMutableList()  // freeze
```

### Cache Logger Level Checks

```kotlin
// Good — avoids string interpolation when debug is disabled
if (HsLog.isDebugEnabled()) {
    HsLog.debug("State: {}", expensiveDump())
}

// Bad — always evaluates the string
HsLog.debug("State: {}", expensiveDump())
```

### Use `HsExecutor.virtualThreads()` on JDK 21+

Virtual threads are ideal for I/O-bound workloads:

```kotlin
val executor = HsExecutor.virtualThreads()
val results = urls.map { url ->
    executor.submit { client.get(url) }
}
```

### Profile with `HsProfiler`

```kotlin
HsProfiler.measure("json-parse") { parseJson(data) }
HsProfiler.measure("db-query") { queryDatabase(params) }
HsIO.println(HsProfiler.report())
```

---

## 8. Migration from JVM Standard Library

### Type Mapping

| JVM / Kotlin | HASAB Runtime |
|---|---|
| `String` | `String` (direct), `HsString` (utilities) |
| `Int` | `Int` (direct), `HsInt` (utilities) |
| `List<T>` | `HsList` |
| `MutableList<T>` | `HsMutableList` |
| `Map<K, V>` | `HsMap` |
| `MutableMap<K, V>` | `HsMutableMap` |
| `Set<T>` | `HsSet` |
| `MutableSet<T>` | `HsMutableSet` |
| `java.io.File` | `HsFile` or `HsDirectory` |
| `java.net.URL` | `HsUrl` |
| `java.net.HttpURLConnection` | `HsHttpClient` |
| `java.time.*` | `HsDateTime` |
| `Thread` | `HsThread` |
| `synchronized` / `ReentrantLock` | `HsLock` |
| `CompletableFuture` | `HsFuture` |
| `ExecutorService` | `HsExecutor` |
| `System.out.println` | `HsIO.println` |
| `java.util.logging` | `HsLog` |
| `System.getProperty` | `HsPlatform.getSystemProperty` |

### Example Migration

**Before (JVM):**

```kotlin
import java.io.File
import java.net.URL

val lines = File("data.txt").readLines()
val url = URL("https://api.example.com/data")
val response = url.readText()
```

**After (HASAB):**

```kotlin
import hasab.runtime.io.HsFile
import hasab.runtime.network.HsHttpClient
import hasab.runtime.io.HsIO

val file = HsFile("data.txt")
val lines = file.readAllLines()

val client = HsHttpClient()
val response = client.get("https://api.example.com/data")
val data = response.bodyAsString()
```

### Interop with JVM Code

All HASAB collection types expose their JVM backing type:

```kotlin
val hsList = HsList.of(1, 2, 3)
val jvmList: List<Any?> = hsList.javaList    // pass to JVM APIs

val jvmMap = mapOf("a" to 1)
val hsMap = HsMap.fromJavaMap(jvmMap)        // wrap JVM collections
```

---

## 9. API Stability Guarantees

| Stability Level | Meaning |
|---|---|
| **Stable** | Will not change in breaking ways. Safe to use in production. |
| **Experimental** | May change in minor versions. Marked with `@HsExperimentalApi`. |
| **Internal** | Not part of the public API. Marked with `@HsInternalApi`. |

**Guarantees:**

- All `public` APIs in `hasab.runtime.*` are Stable unless annotated otherwise.
- Breaking changes require a major version bump (semver).
- Deprecated APIs will be maintained for at least one major version after deprecation.
- `@HsExperimentalApi` APIs may change without notice in minor versions.
- `@HsInternalApi` APIs are for internal use only and may change at any time.

**Versioning:**

```
MAJOR.MINOR.PATCH
  ^     ^     ^
  |     |     └── Bug fixes (no API changes)
  |     └──────── New features (backward-compatible)
  └────────────── Breaking changes
```

---

## 10. Future Roadmap

### Native Backend (Kotlin/Native)

Future versions will target Kotlin/Native for direct compilation to:

- **Linux x86_64/aarch64** — native executables
- **macOS x86_64/aarch64** — native executables
- **Windows x86_64** — native executables
- **iOS arm64** — mobile applications

The `HsPlatform` module will automatically detect and expose the target platform. JVM-specific modules like `HsReflection` and `HsConcurrency` (thread-based) will provide native alternatives using Kotlin/Native's concurrency model (coroutines, atomic references, freeze semantics).

### WebAssembly Backend (WASM)

A WASM target will enable HASAB programs to run in:

- **Web browsers** — via WASM compilation
- **Node.js** — server-side WASM execution
- **Edge runtimes** — Cloudflare Workers, Deno Deploy

Module-specific WASM adaptations:

| Module | JVM | WASM |
|---|---|---|
| `core` | JVM primitives | Direct WASM primitives |
| `collections` | JVM collections | Custom WASM implementations |
| `io` | Console/streams | Browser APIs / fetch |
| `filesystem` | `java.io.File` | In-memory FS / OPFS |
| `network` | `HttpURLConnection` | `fetch()` API |
| `concurrency` | Threads | Web Workers / SharedArrayBuffer |
| `reflection` | JVM reflection | Type metadata tables |
| `math` | `kotlin.math` | WASM math intrinsics |

### Planned Additions

- `hasab.runtime.crypto` — Hashing, signing, encryption
- `hasab.runtime.json` — JSON serialization/deserialization
- `hasab.runtime.regex` — Enhanced regex with named groups and replaceAll
- `hasab.runtime.test` — Built-in testing utilities
- `hasab.runtime.cli` — Command-line argument parsing
- `hasab.runtime.logging` — Structured logging with appenders

---

*This guide covers HASAB Runtime v1.0.0. Last updated: 2026-07-19.*
