# HASAB Runtime & Standard Library - UML Diagrams

PlantUML diagrams documenting the HASAB runtime architecture.

---

## 1. Class Diagram — All 14 Modules

```plantuml
@startuml HASAB_Class_Diagram
skinparam linetype ortho
skinparam classAttributeIconSize 0
title HASAB Runtime & Standard Library — Class Diagram

package "hasab.runtime.core" {
  abstract class HsObject {
    +getType(): HsType
    +toString(): HsString
    +equals(other: HsObject): HsBool
    +hashCode(): HsInt
    +clone(): HsObject
    +isNull(): HsBool
  }
  class HsString {
    -value: String
    +length(): HsInt
    +charAt(index: HsInt): HsChar
    +substring(start: HsInt, end: HsInt): HsString
    +toUpperCase(): HsString
    +toLowerCase(): HsString
    +trim(): HsString
    +split(delimiter: HsString): HsArray
    +contains(other: HsString): HsBool
    +startsWith(prefix: HsString): HsBool
    +endsWith(suffix: HsString): HsBool
    +indexOf(other: HsString): HsInt
    +replace(target: HsString, replacement: HsString): HsString
    +toCharArray(): HsArray
    +isEmpty(): HsBool
    +reverse(): HsString
  }
  class HsInt {
    -value: long
    +add(other: HsInt): HsInt
    +subtract(other: HsInt): HsInt
    +multiply(other: HsInt): HsInt
    +divide(other: HsInt): HsInt
    +modulo(other: HsInt): HsInt
    +negate(): HsInt
    +abs(): HsInt
    +compareTo(other: HsInt): HsInt
    +toFloat(): HsFloat
    +toString(): HsString
    +isEven(): HsBool
    +isOdd(): HsBool
  }
  class HsFloat {
    -value: double
    +add(other: HsFloat): HsFloat
    +subtract(other: HsFloat): HsFloat
    +multiply(other: HsFloat): HsFloat
    +divide(other: HsFloat): HsFloat
    +negate(): HsFloat
    +abs(): HsFloat
    +ceil(): HsInt
    +floor(): HsInt
    +round(): HsInt
    +isNaN(): HsBool
    +isInfinite(): HsBool
    +compareTo(other: HsFloat): HsInt
    +toString(): HsString
    +toInt(): HsInt
  }
  class HsChar {
    -value: char
    +isLetter(): HsBool
    +isDigit(): HsBool
    +isWhitespace(): HsBool
    +toUpperCase(): HsChar
    +toLowerCase(): HsChar
    +toInt(): HsInt
    +toString(): HsString
    +compareTo(other: HsChar): HsInt
  }
  class HsBool {
    -value: boolean
    +and(other: HsBool): HsBool
    +or(other: HsBool): HsBool
    +not(): HsBool
    +toString(): HsString
    +toInt(): HsInt
  }
  class HsArray {
    -elements: Object[]
    +length(): HsInt
    +get(index: HsInt): HsObject
    +set(index: HsInt, value: HsObject): void
    +add(element: HsObject): void
    +remove(index: HsInt): HsObject
    +contains(element: HsObject): HsBool
    +indexOf(element: HsObject): HsInt
    +slice(start: HsInt, end: HsInt): HsArray
    +map(fn: HsObject): HsArray
    +filter(fn: HsObject): HsArray
    +reduce(fn: HsObject, init: HsObject): HsObject
    +forEach(fn: HsObject): void
    +sort(fn: HsObject): HsArray
    +reverse(): HsArray
    +isEmpty(): HsBool
    +toList(): HsList
  }
  HsObject <|-- HsString
  HsObject <|-- HsInt
  HsObject <|-- HsFloat
  HsObject <|-- HsChar
  HsObject <|-- HsBool
  HsObject <|-- HsArray
}

package "hasab.runtime.collections" {
  class HsList {
    -elements: ImmutableList
    +length(): HsInt
    +get(index: HsInt): HsObject
    +add(element: HsObject): HsList
    +remove(index: HsInt): HsList
    +contains(element: HsObject): HsBool
    +indexOf(element: HsObject): HsInt
    +slice(start: HsInt, end: HsInt): HsList
    +map(fn: HsObject): HsList
    +filter(fn: HsObject): HsList
    +reduce(fn: HsObject, init: HsObject): HsObject
    +forEach(fn: HsObject): void
    +sort(fn: HsObject): HsList
    +reverse(): HsList
    +prepend(element: HsObject): HsList
    +append(element: HsObject): HsList
    +concat(other: HsList): HsList
    +isEmpty(): HsBool
    +toMutable(): HsMutableList
  }
  class HsMutableList {
    -elements: MutableList
    +length(): HsInt
    +get(index: HsInt): HsObject
    +set(index: HsInt, value: HsObject): void
    +add(element: HsObject): void
    +remove(index: HsInt): HsObject
    +contains(element: HsObject): HsBool
    +indexOf(element: HsObject): HsInt
    +clear(): void
    +sort(fn: HsObject): void
    +reverse(): void
    +addAll(other: HsList): void
    +isEmpty(): HsBool
    +toImmutable(): HsList
  }
  class HsMap {
    -entries: ImmutableMap
    +size(): HsInt
    +get(key: HsObject): HsObject
    +containsKey(key: HsObject): HsBool
    +containsValue(value: HsObject): HsBool
    +keys(): HsList
    +values(): HsList
    +entries(): HsList
    +put(key: HsObject, value: HsObject): HsMap
    +remove(key: HsObject): HsMap
    +merge(other: HsMap): HsMap
    +forEach(fn: HsObject): void
    +isEmpty(): HsBool
    +toMutable(): HsMutableMap
  }
  class HsMutableMap {
    -entries: MutableMap
    +size(): HsInt
    +get(key: HsObject): HsObject
    +put(key: HsObject, value: HsObject): void
    +remove(key: HsObject): HsObject
    +containsKey(key: HsObject): HsBool
    +containsValue(value: HsObject): HsBool
    +keys(): HsList
    +values(): HsList
    +putAll(other: HsMap): void
    +clear(): void
    +isEmpty(): HsBool
    +toImmutable(): HsMap
  }
  class HsSet {
    -elements: ImmutableSet
    +size(): HsInt
    +contains(element: HsObject): HsBool
    +add(element: HsObject): HsSet
    +remove(element: HsObject): HsSet
    +union(other: HsSet): HsSet
    +intersection(other: HsSet): HsSet
    +difference(other: HsSet): HsSet
    +isSubsetOf(other: HsSet): HsBool
    +isEmpty(): HsBool
    +toList(): HsList
    +toMutable(): HsMutableSet
  }
  class HsMutableSet {
    -elements: MutableSet
    +size(): HsInt
    +contains(element: HsObject): HsBool
    +add(element: HsObject): void
    +remove(element: HsObject): void
    +clear(): void
    +addAll(other: HsSet): void
    +isEmpty(): HsBool
    +toList(): HsList
    +toImmutable(): HsSet
  }
  class HsStack {
    -elements: Deque
    +push(element: HsObject): void
    +pop(): HsObject
    +peek(): HsObject
    +size(): HsInt
    +isEmpty(): HsBool
    +contains(element: HsObject): HsBool
    +clear(): void
    +toList(): HsList
  }
  class HsQueue {
    -elements: Deque
    +enqueue(element: HsObject): void
    +dequeue(): HsObject
    +peek(): HsObject
    +size(): HsInt
    +isEmpty(): HsBool
    +contains(element: HsObject): HsBool
    +clear(): void
    +toList(): HsList
  }
  HsObject <|-- HsList
  HsObject <|-- HsMutableList
  HsObject <|-- HsMap
  HsObject <|-- HsMutableMap
  HsObject <|-- HsSet
  HsObject <|-- HsMutableSet
  HsObject <|-- HsStack
  HsObject <|-- HsQueue
}

package "hasab.runtime.io" {
  class HsIO {
    +readLine(): HsString
    +readLine(prompt: HsString): HsString
    +write(value: HsObject): void
    +writeLine(value: HsObject): void
    +writeError(value: HsObject): void
    +read(): HsString
    +readAll(): HsString
    +readFile(path: HsString): HsString
    +writeFile(path: HsString, content: HsString): void
    +readBytes(path: HsString): HsArray
    +writeBytes(path: HsString, data: HsArray): void
  }
  class HsFile {
    #handle: File
    +getPath(): HsString
    +getName(): HsString
    +getExtension(): HsString
    +exists(): HsBool
    +isFile(): HsBool
    +isDirectory(): HsBool
    +size(): HsInt
    +lastModified(): HsDateTime
    +readText(): HsString
    +writeText(content: HsString): void
    +readBytes(): HsArray
    +writeBytes(data: HsArray): void
    +delete(): HsBool
    +renameTo(newPath: HsString): HsBool
    +copyTo(dest: HsString): HsFile
    +getParent(): HsFile
  }
  HsObject <|-- HsIO
  HsObject <|-- HsFile
}

package "hasab.runtime.text" {
  class HsText {
    -content: String
    +length(): HsInt
    +charAt(index: HsInt): HsChar
    +substring(start: HsInt, end: HsInt): HsText
    +split(delimiter: HsString): HsList
    +join(parts: HsList, delimiter: HsString): HsText
    +trim(): HsText
    +toUpperCase(): HsText
    +toLowerCase(): HsText
    +contains(other: HsText): HsBool
    +replace(target: HsText, replacement: HsText): HsText
    +matches(regex: HsString): HsBool
    +toHsString(): HsString
    +isEmpty(): HsBool
    +reverse(): HsText
    +format(args: HsArray): HsText
    +encode(encoding: HsString): HsArray
    +decode(data: HsArray, encoding: HsString): HsText
  }
  HsObject <|-- HsText
}

package "hasab.runtime.math" {
  class HsMath {
    +{static} abs(x: HsInt): HsInt
    +{static} abs(x: HsFloat): HsFloat
    +{static} min(a: HsInt, b: HsInt): HsInt
    +{static} max(a: HsInt, b: HsInt): HsInt
    +{static} sqrt(x: HsFloat): HsFloat
    +{static} pow(base: HsFloat, exp: HsFloat): HsFloat
    +{static} log(x: HsFloat): HsFloat
    +{static} log2(x: HsFloat): HsFloat
    +{static} log10(x: HsFloat): HsFloat
    +{static} sin(x: HsFloat): HsFloat
    +{static} cos(x: HsFloat): HsFloat
    +{static} tan(x: HsFloat): HsFloat
    +{static} asin(x: HsFloat): HsFloat
    +{static} acos(x: HsFloat): HsFloat
    +{static} atan(x: HsFloat): HsFloat
    +{static} atan2(y: HsFloat, x: HsFloat): HsFloat
    +{static} floor(x: HsFloat): HsInt
    +{static} ceil(x: HsFloat): HsInt
    +{static} round(x: HsFloat): HsInt
    +{static} random(): HsFloat
    +{static} PI: HsFloat
    +{static} E: HsFloat
  }
}

package "hasab.runtime.datetime" {
  class HsDateTime {
    -timestamp: Instant
    +now(): HsDateTime
    +of(year: HsInt, month: HsInt, day: HsInt): HsDateTime
    +of(year: HsInt, month: HsInt, day: HsInt, hour: HsInt, min: HsInt, sec: HsInt): HsDateTime
    +parse(iso: HsString): HsDateTime
    +getYear(): HsInt
    +getMonth(): HsInt
    +getDay(): HsInt
    +getHour(): HsInt
    +getMinute(): HsInt
    +getSecond(): HsInt
    +plusDays(days: HsInt): HsDateTime
    +plusHours(hours: HsInt): HsDateTime
    +plusMinutes(minutes: HsInt): HsDateTime
    +minusDays(days: HsInt): HsDateTime
    +minusHours(hours: HsInt): HsDateTime
    +diff(other: HsDateTime): HsDateTime
    +isBefore(other: HsDateTime): HsBool
    +isAfter(other: HsDateTime): HsBool
    +toIsoString(): HsString
    +toEpochSeconds(): HsInt
    +format(pattern: HsString): HsString
  }
  HsObject <|-- HsDateTime
}

package "hasab.runtime.exceptions" {
  class HsException {
    -message: String
    -cause: Throwable
    +getMessage(): HsString
    +getCause(): HsException
    +getStackTrace(): HsArray
    +toString(): HsString
  }
  class HsTypeError {
    +HsTypeError(expected: HsString, actual: HsString)
  }
  class HsValueError {
    +HsValueError(message: HsString)
  }
  class HsIndexError {
    +HsIndexError(index: HsInt, size: HsInt)
  }
  class HsKeyError {
    +HsKeyError(key: HsObject)
  }
  class HsNameError {
    +HsNameError(name: HsString)
  }
  class HsIOError {
    +HsIOError(message: HsString, path: HsString)
  }
  class HsFileNotFoundError {
    +HsFileNotFoundError(path: HsString)
  }
  class HsPermissionError {
    +HsPermissionError(path: HsString)
  }
  class HsArithmeticError {
    +HsArithmeticError(message: HsString)
  }
  class HsNotImplementedError {
    +HsNotImplementedError(feature: HsString)
  }
  class HsConcurrencyError {
    +HsConcurrencyError(message: HsString)
  }
  class HsTimeoutError {
    +HsTimeoutError(timeout: HsInt, operation: HsString)
  }
  Exception <|-- HsException
  HsException <|-- HsTypeError
  HsException <|-- HsValueError
  HsException <|-- HsIndexError
  HsException <|-- HsKeyError
  HsException <|-- HsNameError
  HsException <|-- HsIOError
  HsException <|-- HsArithmeticError
  HsException <|-- HsNotImplementedError
  HsException <|-- HsConcurrencyError
  HsException <|-- HsTimeoutError
  HsIOError <|-- HsFileNotFoundError
  HsIOError <|-- HsPermissionError
}

package "hasab.runtime.concurrency" {
  class HsThread {
    -thread: Thread
    +start(): void
    +join(): void
    +join(timeout: HsInt): HsBool
    +isAlive(): HsBool
    +getName(): HsString
    +setName(name: HsString): void
    +getId(): HsInt
    +isDaemon(): HsBool
    +setDaemon(daemon: HsBool): void
    +interrupt(): void
    +isInterrupted(): HsBool
    +sleep(ms: HsInt): void
    +{static} currentThread(): HsThread
    +{static} yield(): void
  }
  class HsLock {
    -lock: ReentrantLock
    +lock(): void
    +tryLock(): HsBool
    +tryLock(timeout: HsInt): HsBool
    +unlock(): void
    +isLocked(): HsBool
    +getHoldCount(): HsInt
    +newCondition(): HsCondition
  }
  class HsFuture {
    -future: CompletableFuture
    +get(): HsObject
    +get(timeout: HsInt): HsObject
    +isDone(): HsBool
    +isCancelled(): HsBool
    +cancel(mayInterrupt: HsBool): HsBool
    +thenApply(fn: HsObject): HsFuture
    +thenAccept(fn: HsObject): HsFuture
    +exceptionally(fn: HsObject): HsFuture
  }
  class HsExecutor {
    -executor: ExecutorService
    +submit(task: HsObject): HsFuture
    +execute(task: HsObject): void
    +shutdown(): void
    +shutdownNow(): HsList
    +isShutdown(): HsBool
    +awaitTermination(timeout: HsInt): HsBool
    +{static} newFixedThreadPool(threads: HsInt): HsExecutor
    +{static} newCachedThreadPool(): HsExecutor
    +{static} newSingleThreadExecutor(): HsExecutor
    +{static} newScheduledThreadPool(threads: HsInt): HsExecutor
  }
  HsObject <|-- HsThread
  HsObject <|-- HsLock
  HsObject <|-- HsFuture
  HsObject <|-- HsExecutor
}

package "hasab.runtime.reflection" {
  class HsReflection {
    +{static} getType(obj: HsObject): HsType
    +{static} getClassName(obj: HsObject): HsString
    +{static} getSuperclass(obj: HsObject): HsType
    +{static} getInterfaces(obj: HsObject): HsList
    +{static} getMethods(obj: HsObject): HsList
    +{static} getFields(obj: HsObject): HsList
    +{static} invoke(obj: HsObject, methodName: HsString, args: HsArray): HsObject
    +{static} getField(obj: HsObject, fieldName: HsString): HsObject
    +{static} setField(obj: HsObject, fieldName: HsString, value: HsObject): void
    +{static} isInstance(obj: HsObject, typeName: HsString): HsBool
    +{static} newInstance(typeName: HsString, args: HsArray): HsObject
  }
}

package "hasab.runtime.annotations" {
  class HsDeprecated {
    +reason: HsString
    +since: HsString
  }
  class HsOverride
  class HsInternal
  class HsPure
  class HsInline
  class HsTailRec
  class HsOperator {
    +precedence: HsInt
    +associativity: HsString
  }
  class HsOverload
  class HsSealed
  class HsVisible
  class HsJvmStatic
  class HsJvmName {
    +name: HsString
  }
}

package "hasab.runtime.services" {
  class HsVersion {
    +{static} current(): HsString
    +{static} major(): HsInt
    +{static} minor(): HsInt
    +{static} patch(): HsInt
    +{static} isAtLeast(version: HsString): HsBool
  }
  class HsProfiler {
    +{static} start(label: HsString): void
    +{static} stop(label: HsString): HsFloat
    +{static} snapshot(): HsMap
    +{static} reset(): void
    +{static} gc(): void
    +{static} memoryUsage(): HsMap
    +{static} threadCount(): HsInt
  }
}

package "hasab.runtime.internal" {
  class HsInternal {
    +{static} identityHash(obj: HsObject): HsInt
    +{static} objectSize(obj: HsObject): HsInt
    +{static} arrayCopy(src: HsArray, srcPos: HsInt, dest: HsArray, destPos: HsInt, len: HsInt): void
    +{static} defineClass(name: HsString, bytecode: HsArray): void
    +{static} unsafeGetField(obj: HsObject, fieldName: HsString): HsObject
    +{static} unsafeSetField(obj: HsObject, fieldName: HsString, value: HsObject): void
  }
}

package "hasab.runtime.util" {
  class HsPlatform {
    +{static} os(): HsString
    +{static} arch(): HsString
    +{static} javaVersion(): HsString
    +{static} cpus(): HsInt
    +{static} totalMemory(): HsInt
    +{static} freeMemory(): HsInt
    +{static} maxMemory(): HsInt
    +{static} env(name: HsString): HsString
    +{static} currentTimeMillis(): HsInt
    +{static} nanoTime(): HsInt
  }
  class HsLog {
    +{static} info(message: HsString): void
    +{static} warn(message: HsString): void
    +{static} error(message: HsString): void
    +{static} debug(message: HsString): void
    +{static} setLevel(level: HsString): void
    +{static} getLevel(): HsString
  }
  class HsConfig {
    -config: Map
    +{static} get(key: HsString): HsString
    +{static} get(key: HsString, default: HsString): HsString
    +{static} set(key: HsString, value: HsString): void
    +{static} load(path: HsString): void
    +{static} save(path: HsString): void
    +{static} keys(): HsList
  }
  class HsLocalize {
    +{static} translate(key: HsString): HsString
    +{static} translate(key: HsString, args: HsArray): HsString
    +{static} setLocale(locale: HsString): void
    +{static} getLocale(): HsString
    +{static} availableLocales(): HsList
  }
}

package "hasab.runtime.filesystem [NEW]" {
  class HsPath {
    -segments: List
    +resolve(relative: HsString): HsPath
    +resolve(other: HsPath): HsPath
    +relativize(other: HsPath): HsPath
    +normalize(): HsPath
    +getParent(): HsPath
    +getFileName(): HsString
    +getExtension(): HsString
    +isAbsolute(): HsBool
    +toAbsolute(): HsPath
    +toString(): HsString
    +startsWith(other: HsPath): HsBool
    +endsWith(other: HsString): HsBool
  }
  class HsDirectory {
    #path: java.io.File
    +list(): HsList
    +listFiles(): HsList
    +listDirs(): HsList
    +walk(): HsList
    +walk(maxDepth: HsInt): HsList
    +create(): HsBool
    +createParents(): HsBool
    +exists(): HsBool
    +delete(): HsBool
    +deleteRecursively(): HsBool
    +copyTo(dest: HsPath): HsDirectory
    +moveTo(dest: HsPath): HsDirectory
    +getPath(): HsPath
    +size(): HsInt
  }
  class HsFileWatcher {
    -directory: HsDirectory
    -callback: HsObject
    -running: boolean
    +start(): void
    +stop(): void
    +isRunning(): HsBool
    +getWatchedPath(): HsPath
  }
  class HsWatchEvent {
    -type: String
    -path: HsPath
    -timestamp: HsDateTime
    +getType(): HsString
    +getPath(): HsPath
    +getTimestamp(): HsDateTime
    +isCreate(): HsBool
    +isModify(): HsBool
    +isDelete(): HsBool
  }
  class HsTempFile {
    #tempFile: java.io.File
    +getPath(): HsPath
    +exists(): HsBool
    +delete(): HsBool
    +getPathString(): HsString
    +{static} create(): HsTempFile
    +{static} create(prefix: HsString): HsTempFile
    +{static} createInDir(dir: HsPath): HsTempFile
    +{static} createTempDir(): HsTempFile
  }
  HsObject <|-- HsPath
  HsObject <|-- HsDirectory
  HsObject <|-- HsFileWatcher
  HsObject <|-- HsWatchEvent
  HsObject <|-- HsTempFile
  HsDirectory --> HsPath : returns
  HsFileWatcher --> HsWatchEvent : emits
  HsFileWatcher --> HsDirectory : watches
}

package "hasab.runtime.network [NEW]" {
  class HsUrl {
    -uri: java.net.URI
    -url: java.net.URL
    +{static} parse(url: HsString): HsUrl
    +{static} build(scheme: HsString, host: HsString, port: HsInt, path: HsString): HsUrl
    +getScheme(): HsString
    +getHost(): HsString
    +getPort(): HsInt
    +getPath(): HsString
    +getQuery(): HsString
    +getFragment(): HsString
    +getUsername(): HsString
    +withPath(path: HsString): HsUrl
    +withQuery(query: HsString): HsUrl
    +withFragment(fragment: HsString): HsUrl
    +resolve(relative: HsString): HsUrl
    +toUri(): HsString
    +toString(): HsString
    +equals(other: HsObject): HsBool
  }
  class HsHttpRequest {
    -method: String
    -url: HsUrl
    -headers: HsMutableMap
    -body: HsArray
    -timeout: HsInt
    +{static} get(url: HsString): HsHttpRequest
    +{static} post(url: HsString, body: HsArray): HsHttpRequest
    +{static} put(url: HsString, body: HsArray): HsHttpRequest
    +{static} delete(url: HsString): HsHttpRequest
    +{static} patch(url: HsString, body: HsArray): HsHttpRequest
    +getMethod(): HsString
    +getUrl(): HsUrl
    +getHeaders(): HsMutableMap
    +getBody(): HsArray
    +getTimeout(): HsInt
    +header(name: HsString, value: HsString): HsHttpRequest
    +timeout(ms: HsInt): HsHttpRequest
    +body(body: HsArray): HsHttpRequest
    +bodyJson(json: HsString): HsHttpRequest
    +build(): HsHttpRequest
  }
  class HsHttpResponse {
    -statusCode: int
    -headers: HsMutableMap
    -body: HsArray
    +getStatusCode(): HsInt
    +getHeaders(): HsMutableMap
    +getBody(): HsArray
    +getBodyText(): HsString
    +getBodyJson(): HsString
    +isSuccess(): HsBool
    +header(name: HsString): HsString
  }
  class HsHttpClient {
    #client: HttpClient
    +execute(request: HsHttpRequest): HsHttpResponse
    +executeAsync(request: HsHttpRequest): HsFuture
    +close(): void
    +{static} create(): HsHttpClient
    +{static} create(timeout: HsInt): HsHttpClient
  }
  HsObject <|-- HsUrl
  HsObject <|-- HsHttpRequest
  HsObject <|-- HsHttpResponse
  HsObject <|-- HsHttpClient
  HsHttpClient --> HsHttpRequest : sends
  HsHttpClient --> HsHttpResponse : returns
  HsHttpClient --> HsFuture : async returns
}

@enduml
```

---

## 2. Module Dependency Diagram

```plantuml
@startuml HASAB_Module_Dependencies
skinparam packageStyle rectangle
title HASAB Runtime — Module Dependencies

package "hasab.runtime.core" as core #LightBlue {
}

package "hasab.runtime.collections" as collections #LightGreen {
}

package "hasab.runtime.io" as io #LightYellow {
}

package "hasab.runtime.text" as text #LightCoral {
}

package "hasab.runtime.math" as math #Lavender {
}

package "hasab.runtime.datetime" as datetime #PeachPuff {
}

package "hasab.runtime.exceptions" as exceptions #MistyRose {
}

package "hasab.runtime.concurrency" as concurrency #PaleGreen {
}

package "hasab.runtime.reflection" as reflection #LightSteelBlue {
}

package "hasab.runtime.annotations" as annotations #Wheat {
}

package "hasab.runtime.services" as services #Thistle {
}

package "hasab.runtime.internal" as internal #Silver {
}

package "hasab.runtime.util" as util #Honeydew {
}

package "hasab.runtime.filesystem" as filesystem #PaleTurquoise {
}

package "hasab.runtime.network" as network #LightPink {
}

' --- Core is the foundation — everything depends on it ---
collections --> core
io --> core
text --> core
math --> core
datetime --> core
exceptions --> core
concurrency --> core
reflection --> core
util --> core

' --- Module-to-module dependencies ---
io --> exceptions : throws
io --> datetime : timestamps
filesystem --> core
filesystem --> io : read/write
filesystem --> exceptions : throws
filesystem --> datetime : last modified

network --> core
network --> io : HTTP streams
network --> exceptions : throws
network --> concurrency : async

text --> core
text --> io : encode/decode

concurrency --> exceptions : throws
concurrency --> util : platform info

reflection --> exceptions : throws
reflection --> util : type registry

services --> util : platform metrics
services --> core

annotations ..> core : metadata only

internal --> core : raw access

util --> core

@enduml
```

---

## 3. Exception Hierarchy

```plantuml
@startuml HASAB_Exception_Hierarchy
skinparam classAttributeIconSize 0
title HASAB Exception Hierarchy

class Exception <<Java>> {
  +getMessage(): String
  +getCause(): Throwable
  +printStackTrace(): void
}

class HsException {
  +getMessage(): HsString
  +getCause(): HsException
  +getStackTrace(): HsArray
  +toString(): HsString
}

class HsTypeError {
  -expected: HsString
  -actual: HsString
  +HsTypeError(expected: HsString, actual: HsString)
}

class HsValueError {
  +HsValueError(message: HsString)
}

class HsIndexError {
  -index: HsInt
  -size: HsInt
  +HsIndexError(index: HsInt, size: HsInt)
}

class HsKeyError {
  -key: HsObject
  +HsKeyError(key: HsObject)
}

class HsNameError {
  -name: HsString
  +HsNameError(name: HsString)
}

class HsArithmeticError {
  +HsArithmeticError(message: HsString)
}

class HsNotImplementedError {
  -feature: HsString
  +HsNotImplementedError(feature: HsString)
}

class HsConcurrencyError {
  +HsConcurrencyError(message: HsString)
}

class HsTimeoutError {
  -timeout: HsInt
  -operation: HsString
  +HsTimeoutError(timeout: HsInt, operation: HsString)
}

class HsIOError {
  -path: HsString
  +HsIOError(message: HsString, path: HsString)
}

class HsFileNotFoundError {
  -path: HsString
  +HsFileNotFoundError(path: HsString)
}

class HsPermissionError {
  -path: HsString
  +HsPermissionError(path: HsString)
}

Exception <|-- HsException : extends

HsException <|-- HsTypeError
HsException <|-- HsValueError
HsException <|-- HsIndexError
HsException <|-- HsKeyError
HsException <|-- HsNameError
HsException <|-- HsArithmeticError
HsException <|-- HsNotImplementedError
HsException <|-- HsConcurrencyError
HsException <|-- HsTimeoutError
HsException <|-- HsIOError

HsIOError <|-- HsFileNotFoundError
HsIOError <|-- HsPermissionError

note bottom of HsException
  Base exception for all HASAB
  runtime errors. Maps to JVM
  Exception class.
end note

note right of HsFileNotFoundError
  File system operations
  that reference missing
  paths or directories.
end note

note right of HsTimeoutError
  Futures, locks, and I/O
  operations with deadlines.
end note

@enduml
```

---

## 4. Collections Hierarchy — Immutable vs Mutable

```plantuml
@startuml HASAB_Collections_Hierarchy
skinparam classAttributeIconSize 0
title HASAB Collections — Immutable vs Mutable Types

class HsObject {
  +getType(): HsType
  +toString(): HsString
  +equals(other: HsObject): HsBool
  +hashCode(): HsInt
}

package "Immutable Collections" #LightBlue {
  class HsList {
    +length(): HsInt
    +get(index: HsInt): HsObject
    +add(element: HsObject): HsList
    +remove(index: HsInt): HsList
    +contains(element: HsObject): HsBool
    +map(fn: HsObject): HsList
    +filter(fn: HsObject): HsList
    +reduce(fn: HsObject, init: HsObject): HsObject
    +sort(fn: HsObject): HsList
    +reverse(): HsList
    +prepend(element: HsObject): HsList
    +append(element: HsObject): HsList
    +concat(other: HsList): HsList
    +isEmpty(): HsBool
    +toMutable(): HsMutableList
  }
  class HsMap {
    +size(): HsInt
    +get(key: HsObject): HsObject
    +containsKey(key: HsObject): HsBool
    +keys(): HsList
    +values(): HsList
    +entries(): HsList
    +put(key: HsObject, value: HsObject): HsMap
    +remove(key: HsObject): HsMap
    +merge(other: HsMap): HsMap
    +isEmpty(): HsBool
    +toMutable(): HsMutableMap
  }
  class HsSet {
    +size(): HsInt
    +contains(element: HsObject): HsBool
    +add(element: HsObject): HsSet
    +remove(element: HsObject): HsSet
    +union(other: HsSet): HsSet
    +intersection(other: HsSet): HsSet
    +difference(other: HsSet): HsSet
    +isSubsetOf(other: HsSet): HsBool
    +isEmpty(): HsBool
    +toMutable(): HsMutableSet
  }
}

package "Mutable Collections" #LightGreen {
  class HsMutableList {
    +length(): HsInt
    +get(index: HsInt): HsObject
    +set(index: HsInt, value: HsObject): void
    +add(element: HsObject): void
    +remove(index: HsInt): HsObject
    +contains(element: HsObject): HsBool
    +clear(): void
    +sort(fn: HsObject): void
    +reverse(): void
    +addAll(other: HsList): void
    +isEmpty(): HsBool
    +toImmutable(): HsList
  }
  class HsMutableMap {
    +size(): HsInt
    +get(key: HsObject): HsObject
    +put(key: HsObject, value: HsObject): void
    +remove(key: HsObject): HsObject
    +containsKey(key: HsObject): HsBool
    +keys(): HsList
    +values(): HsList
    +putAll(other: HsMap): void
    +clear(): void
    +isEmpty(): HsBool
    +toImmutable(): HsMap
  }
  class HsMutableSet {
    +size(): HsInt
    +contains(element: HsObject): HsBool
    +add(element: HsObject): void
    +remove(element: HsObject): void
    +clear(): void
    +addAll(other: HsSet): void
    +isEmpty(): HsBool
    +toImmutable(): HsSet
  }
}

package "Queue-like Structures" #Lavender {
  class HsStack {
    +push(element: HsObject): void
    +pop(): HsObject
    +peek(): HsObject
    +size(): HsInt
    +isEmpty(): HsBool
    +contains(element: HsObject): HsBool
    +clear(): void
    +toList(): HsList
  }
  class HsQueue {
    +enqueue(element: HsObject): void
    +dequeue(): HsObject
    +peek(): HsObject
    +size(): HsInt
    +isEmpty(): HsBool
    +contains(element: HsObject): HsBool
    +clear(): void
    +toList(): HsList
  }
}

package "Array (Fixed-size)" #Thistle {
  class HsArray {
    +length(): HsInt
    +get(index: HsInt): HsObject
    +set(index: HsInt, value: HsObject): void
    +add(element: HsObject): void
    +remove(index: HsInt): HsObject
    +contains(element: HsObject): HsBool
    +indexOf(element: HsObject): HsInt
    +slice(start: HsInt, end: HsInt): HsArray
    +map(fn: HsObject): HsArray
    +filter(fn: HsObject): HsArray
    +reduce(fn: HsObject, init: HsObject): HsObject
    +forEach(fn: HsObject): void
    +sort(fn: HsObject): HsArray
    +reverse(): HsArray
    +isEmpty(): HsBool
    +toList(): HsList
  }
}

HsObject <|-- HsList
HsObject <|-- HsMap
HsObject <|-- HsSet
HsObject <|-- HsMutableList
HsObject <|-- HsMutableMap
HsObject <|-- HsMutableSet
HsObject <|-- HsStack
HsObject <|-- HsQueue
HsObject <|-- HsArray

' --- Conversion arrows ---
HsList ..> HsMutableList : toMutable()
HsMutableList ..> HsList : toImmutable()
HsMap ..> HsMutableMap : toMutable()
HsMutableMap ..> HsMap : toImmutable()
HsSet ..> HsMutableSet : toMutable()
HsMutableSet ..> HsSet : toImmutable()
HsArray ..> HsList : toList()
HsStack ..> HsList : toList()
HsQueue ..> HsList : toList()

note right of HsStack
  LIFO — Last In, First Out.
  Backed by Deque.
end note

note right of HsQueue
  FIFO — First In, First Out.
  Backed by Deque.
end note

note bottom of HsArray
  Fixed-size, contiguous storage.
  Supports fast indexed access.
end note

@enduml
```

---

## 5. New Module Detail — filesystem/ and network/

```plantuml
@startuml HASAB_New_Modules_Detail
skinparam classAttributeIconSize 0
skinparam linetype ortho
title HASAB Runtime — New Modules: filesystem/ and network/

' ============================================================
'  FILESYSTEM MODULE
' ============================================================

package "hasab.runtime.filesystem" as fs #PaleTurquoise {

  class HsPath {
    -segments: List<String>
    +resolve(relative: HsString): HsPath
    +resolve(other: HsPath): HsPath
    +relativize(other: HsPath): HsPath
    +normalize(): HsPath
    +getParent(): HsPath
    +getFileName(): HsString
    +getExtension(): HsString
    +isAbsolute(): HsBool
    +toAbsolute(): HsPath
    +toString(): HsString
    +startsWith(other: HsPath): HsBool
    +endsWith(other: HsString): HsBool
  }

  class HsDirectory {
    #path: java.io.File
    +list(): HsList
    +listFiles(): HsList
    +listDirs(): HsList
    +walk(): HsList
    +walk(maxDepth: HsInt): HsList
    +create(): HsBool
    +createParents(): HsBool
    +exists(): HsBool
    +delete(): HsBool
    +deleteRecursively(): HsBool
    +copyTo(dest: HsPath): HsDirectory
    +moveTo(dest: HsPath): HsDirectory
    +getPath(): HsPath
    +size(): HsInt
  }

  class HsFileWatcher {
    -directory: HsDirectory
    -callback: HsObject
    -running: boolean
    -pollInterval: long
    +start(): void
    +stop(): void
    +isRunning(): HsBool
    +getWatchedPath(): HsPath
  }

  class HsWatchEvent {
    -type: EventType
    -path: HsPath
    -timestamp: HsDateTime
    +getType(): HsString
    +getPath(): HsPath
    +getTimestamp(): HsDateTime
    +isCreate(): HsBool
    +isModify(): HsBool
    +isDelete(): HsBool
  }

  enum EventType {
    CREATE
    MODIFY
    DELETE
  }

  class HsTempFile {
    #tempFile: java.io.File
    +getPath(): HsPath
    +exists(): HsBool
    +delete(): HsBool
    +getPathString(): HsString
    +{static} create(): HsTempFile
    +{static} create(prefix: HsString): HsTempFile
    +{static} createInDir(dir: HsPath): HsTempFile
    +{static} createTempDir(): HsTempFile
  }

  ' --- Relationships within filesystem ---
  HsFileWatcher --> HsDirectory : watches
  HsFileWatcher --> HsWatchEvent : emits on change
  HsWatchEvent --> EventType : has
  HsWatchEvent --> HsPath : references
  HsDirectory --> HsPath : getPath()
  HsTempFile ..> HsPath : getPath()
}

' ============================================================
'  NETWORK MODULE
' ============================================================

package "hasab.runtime.network" as net #LightPink {

  class HsUrl {
    -uri: java.net.URI
    -url: java.net.URL
    +{static} parse(url: HsString): HsUrl
    +{static} build(scheme: HsString, host: HsString, port: HsInt, path: HsString): HsUrl
    +getScheme(): HsString
    +getHost(): HsString
    +getPort(): HsInt
    +getPath(): HsString
    +getQuery(): HsString
    +getFragment(): HsString
    +getUsername(): HsString
    +withPath(path: HsString): HsUrl
    +withQuery(query: HsString): HsUrl
    +withFragment(fragment: HsString): HsUrl
    +resolve(relative: HsString): HsUrl
    +toUri(): HsString
    +toString(): HsString
    +equals(other: HsObject): HsBool
  }

  class HsHttpRequest {
    -method: String
    -url: HsUrl
    -headers: HsMutableMap
    -body: HsArray
    -timeout: HsInt
    +{static} get(url: HsString): HsHttpRequest
    +{static} post(url: HsString, body: HsArray): HsHttpRequest
    +{static} put(url: HsString, body: HsArray): HsHttpRequest
    +{static} delete(url: HsString): HsHttpRequest
    +{static} patch(url: HsString, body: HsArray): HsHttpRequest
    +getMethod(): HsString
    +getUrl(): HsUrl
    +getHeaders(): HsMutableMap
    +getBody(): HsArray
    +getTimeout(): HsInt
    +header(name: HsString, value: HsString): HsHttpRequest
    +timeout(ms: HsInt): HsHttpRequest
    +body(body: HsArray): HsHttpRequest
    +bodyJson(json: HsString): HsHttpRequest
    +build(): HsHttpRequest
  }

  class HsHttpResponse {
    -statusCode: int
    -headers: HsMutableMap
    -body: HsArray
    +getStatusCode(): HsInt
    +getHeaders(): HsMutableMap
    +getBody(): HsArray
    +getBodyText(): HsString
    +getBodyJson(): HsString
    +isSuccess(): HsBool
    +header(name: HsString): HsString
  }

  class HsHttpClient {
    #client: java.net.http.HttpClient
    +execute(request: HsHttpRequest): HsHttpResponse
    +executeAsync(request: HsHttpRequest): HsFuture
    +close(): void
    +{static} create(): HsHttpClient
    +{static} create(timeout: HsInt): HsHttpClient
  }

  enum HttpMethod {
    GET
    POST
    PUT
    DELETE
    PATCH
  }

  ' --- Relationships within network ---
  HsHttpClient --> HsHttpRequest : sends
  HsHttpClient --> HsHttpResponse : returns (sync)
  HsHttpClient ..> HsFuture : returns (async)
  HsHttpRequest --> HsUrl : targets
  HsHttpRequest --> HttpMethod : uses method
  HsHttpRequest --> HsMutableMap : carries headers
  HsHttpResponse --> HsMutableMap : exposes headers
}

' ============================================================
'  CROSS-MODULE BRIDGES
' ============================================================

fs ..> net : HsFileWatcher can
       trigger network sync

HsHttpClient ..> HsFuture : wraps in concurrency
HsHttpClient ..> HsException : throws on failure

@enduml
```

---

## 2. CLI Toolchain — Class Diagram

```plantuml
@startuml HASAB_CLI_Class_Diagram
skinparam linetype ortho
skinparam classAttributeIconSize 0
title HASAB CLI Toolchain — Class Diagram

package "hasab.cli" {
  abstract class Command {
    +getName(): String
    +getDescription(): String
    +getUsage(): String
    +run(args: List<String>, projectPath: Path): Int
  }

  class CommandRouter {
    -commands: Map<String, Command>
    +register(command: Command)
    +route(args: List<String>): Int
  }

  class HasabCli {
    -router: CommandRouter
    +main(args: Array<String>)
    +run(args: List<String>): Int
  }

  class Terminal {
    +{static} RED: String
    +{static} GREEN: String
    +{static} YELLOW: String
    +{static} BLUE: String
    +{static} CYAN: String
    +{static} RESET: String
    +{static} BOLD: String
    +{static} printSuccess(msg: String)
    +{static} printError(msg: String)
    +{static} printWarning(msg: String)
    +{static} printInfo(msg: String)
    +{static} printBanner()
    +{static} printTable(headers, rows)
    +{static} withProgressBar(label, action)
  }
}

package "hasab.cli.config" {
  class HasabToml {
    +{static} parse(content: String): ProjectConfig
    +{static} serialize(config: ProjectConfig): String
  }
  class ProjectConfig {
    +name: String
    +version: String
    +description: String
    +haskellVersion: String
    +dependencies: List<String>
    +authors: List<String>
    +license: String
    +repository: String
  }
}

package "hasab.cli.commands" {
  class NewCommand
  class BuildCommand
  class RunCommand
  class TestCommand
  class CleanCommand
  class AddCommand
  class RemoveCommand
  class PublishCommand
  class FmtCommand
  class LintCommand
  class DocCommand
  class HelpCommand
  class VersionCommand
  class DoctorCommand
  class PackageRegistry
}

package "hasab.cli.fmt" {
  class HasabFormatter {
    +format(source: String): String
    -fixIndentation(source: String): String
    -normalizeWhitespace(source: String): String
    -fixOperators(source: String): String
  }
}

package "hasab.cli.lint" {
  class HasabLinter {
    +lint(source: String, rules: List<LintRule>): List<LintIssue>
  }
  class LintRule {
    -name: String
    -description: String
    -severity: LintSeverity
  }
  enum LintSeverity { WARNING, ERROR, INFO }
  class LintResult {
    +issues: List<LintIssue>
    +passed: Boolean
  }
  class LintIssue {
    +rule: LintRule
    +line: Int
    +column: Int
    +message: String
  }
}

package "hasab.cli.docgen" {
  class HasabDocGenerator {
    +generate(source: String, fileName: String): List<DocEntry>
    +toMarkdown(entries: List<DocEntry>): String
  }
  class DocEntry {
    +name: String
    +type: String
    +description: String
    +parameters: List<String>
    +line: Int
  }
}

Command <|-- NewCommand
Command <|-- BuildCommand
Command <|-- RunCommand
Command <|-- TestCommand
Command <|-- CleanCommand
Command <|-- AddCommand
Command <|-- RemoveCommand
Command <|-- PublishCommand
Command <|-- FmtCommand
Command <|-- LintCommand
Command <|-- DocCommand
Command <|-- HelpCommand
Command <|-- VersionCommand
Command <|-- DoctorCommand

HasabCli *--> CommandRouter
CommandRouter o--> Command : manages

ProjectConfig <-- HasabToml : parses/serializes
FmtCommand --> HasabFormatter : delegates
LintCommand --> HasabLinter : delegates
HasabLinter o--> LintRule : applies
LintRule ..> LintSeverity
LintResult *--> LintIssue
DocCommand --> HasabDocGenerator : delegates
HasabDocGenerator ..> DocEntry : produces

AddCommand --> PackageRegistry
RemoveCommand --> PackageRegistry
PublishCommand --> PackageRegistry

@enduml
```

---

## 3. CLI Command Flow — Sequence Diagram

```plantuml
@startuml HASAB_CLI_Sequence
skinparam sequenceMessageAlign center
title HASAB CLI Command Execution Flow

actor User
participant "HasabCli" as CLI
participant "CommandRouter" as Router
participant "Command" as Cmd
participant "Terminal" as Term
participant "ProjectConfig" as Config
participant "FileSystem" as FS

User -> CLI : hasab <command> [args]
activate CLI

CLI -> Router : route(args)
activate Router

Router -> Router : parse command name
Router -> Router : lookup command registry

alt command found
  Router -> Cmd : run(args, projectPath)
  activate Cmd

  Cmd -> Term : printInfo("Running...")
  activate Term
  Term --> Cmd : colored output
  deactivate Term

  Cmd -> FS : read project files
  activate FS
  FS --> Cmd : file contents
  deactivate FS

  Cmd -> Config : load project.toml
  activate Config
  Config --> Cmd : ProjectConfig
  deactivate Config

  Cmd -> Cmd : execute logic

  Cmd -> Term : printSuccess/Error(...)
  activate Term
  Term --> Cmd : colored output
  deactivate Term

  Cmd --> Router : exitCode (0 or 1)
  deactivate Cmd

  Router --> CLI : exitCode
else command not found
  Router -> Term : printError("Unknown command")
  activate Term
  Term --> Router : colored error
  deactivate Term
  Router --> CLI : 1
end

CLI --> User : System.exit(exitCode)
deactivate CLI

@enduml
```

---

## 4. Package Management Flow — Sequence Diagram

```plantuml
@startuml HASAB_Package_Flow
skinparam sequenceMessageAlign center
title HASAB Package Management Flow (add / remove / publish)

actor User
participant "AddCommand" as Add
participant "PackageRegistry" as Reg
participant "ProjectConfig" as Config
participant "HasabToml" as Toml
participant "FileSystem" as FS

== Add Dependency ==
User -> Add : hasab add <package>[@version]
activate Add

Add -> Config : load(project.toml)
activate Config
Config --> Add : ProjectConfig
deactivate Config

Add -> Reg : resolve(package, version)
activate Reg
Reg -> Reg : simulate lookup
Reg --> Add : PackageInfo(name, version)
deactivate Reg

Add -> Add : add to dependencies list
Add -> Config : serialize(updated config)
activate Config
Config --> Add : TOML string
deactivate Config

Add -> FS : write(project.toml, toml)
activate FS
FS --> Add : written
deactivate FS

Add -> User : printSuccess("Added <package>@<version>")
deactivate Add

== Remove Dependency ==
User -> Add : hasab remove <package>
activate Add

Add -> Config : load(project.toml)
Add -> Add : remove from dependencies
Add -> FS : write(project.toml)
Add -> User : printSuccess("Removed <package>")
deactivate Add

== Publish Package ==
participant "PublishCommand" as Pub

User -> Pub : hasab publish
activate Pub

Pub -> Config : load(project.toml)
Pub -> FS : read all source files
Pub -> Pub : collect API surface

Pub -> Reg : register(name, version, files)
activate Reg
Reg -> Reg : simulate publish
Reg --> Pub : success
deactivate Reg

Pub -> User : printSuccess("Published <name>@<version>")
deactivate Pub

@enduml
```

---

## 5. Formatter and Linter — Activity Diagram

```plantuml
@startuml HASAB_Fmt_Lint_Activity
title HASAB Formatter and Linter Processing Flow

|Formatter|
start
:Read source file;
:Tokenize lines;

partition "Fix Indentation" {
  :Detect block openings (colon + newline + brace);
  :Increase indent level;
  :Detect block closings (brace);
  :Decrease indent level;
  :Apply consistent indentation (2 spaces);
}

partition "Normalize Whitespace" {
  :Collapse multiple spaces;
  :Trim trailing whitespace;
  :Ensure single newline at EOF;
}

partition "Fix Operators" {
  :Normalize spacing around =, ==, arrow;
  :Add spaces around keywords;
}

:Write formatted output;
stop

|Linter|
start
:Read source file;

partition "Apply Rules" {
  :Rule: Unused variables;
  :Rule: Empty blocks;
  :Rule: Trailing whitespace;
  :Rule: Long lines (>120 chars);
  :Rule: Missing documentation;
  :Rule: Variable shadowing;
}

partition "Collect Results" {
  :Create LintIssue per violation;
  :Calculate passed = (errors == 0);
  :Group by severity;
}

:Print colored output;
if (passed?) then (yes)
  :printSuccess("All checks passed");
else (no)
  :printError("N issues found");
endif
stop

@enduml
```

---

## 6. Project Structure — Component Diagram

```plantuml
@startuml HASAB_Component_Diagram
skinparam component {
  BackgroundColor<<cli>> LightBlue
  BackgroundColor<<core>> LightGreen
  BackgroundColor<<stdlib>> LightYellow
}
title HASAB Project — Component Overview

package "hasab.parser" <<core>> {
  [Lexer]
  [Parser]
  [AstBuilder]
}

package "hasab.codegen" <<core>> {
  [CodeGenerator]
  [TypeChecker]
}

package "hasab.compiler" <<core>> {
  [HasabToJavaCompiler]
  [CompilationResult]
}

package "hasab.cli" <<cli>> {
  [HasabCli]
  [CommandRouter]
  [Terminal]
}

package "hasab.cli.config" <<cli>> {
  [HasabToml]
  [ProjectConfig]
}

package "hasab.cli.commands" <<cli>> {
  [NewCommand]
  [BuildCommand]
  [RunCommand]
  [TestCommand]
  [CleanCommand]
  [AddCommand]
  [RemoveCommand]
  [PublishCommand]
  [HelpCommand]
  [VersionCommand]
  [DoctorCommand]
}

package "hasab.cli.fmt" <<cli>> {
  [FmtCommand]
  [HasabFormatter]
}

package "hasab.cli.lint" <<cli>> {
  [LintCommand]
  [HasabLinter]
  [LintRules]
}

package "hasab.cli.docgen" <<cli>> {
  [DocCommand]
  [HasabDocGenerator]
}

package "hasab.runtime.core" <<stdlib>> {
  [HsObject]
  [HsString]
  [HsInt]
  [HsBool]
}

[HasabCli] --> [CommandRouter]
[CommandRouter] --> [NewCommand]
[CommandRouter] --> [BuildCommand]
[CommandRouter] --> [RunCommand]
[CommandRouter] --> [TestCommand]
[CommandRouter] --> [CleanCommand]
[CommandRouter] --> [AddCommand]
[CommandRouter] --> [RemoveCommand]
[CommandRouter] --> [PublishCommand]
[CommandRouter] --> [FmtCommand]
[CommandRouter] --> [LintCommand]
[CommandRouter] --> [DocCommand]
[CommandRouter] --> [HelpCommand]
[CommandRouter] --> [VersionCommand]
[CommandRouter] --> [DoctorCommand]

[BuildCommand] --> [HasabToJavaCompiler]
[FmtCommand] --> [HasabFormatter]
[LintCommand] --> [HasabLinter]
[DocCommand] --> [HasabDocGenerator]

@enduml
```

---

## 7. LSP Server Architecture — Component Diagram

```plantuml
@startuml HASAB_LSP_Component_Diagram
skinparam component {
  BackgroundColor<<lsp>> LightBlue
  BackgroundColor<<compiler>> LightGreen
  BackgroundColor<<core>> LightYellow
}
title HASAB Language Server — Component Overview

package "LSP Protocol Layer" <<lsp>> {
  [HasabLanguageServer]
  [HasabTextDocumentService]
  [HasabWorkspaceService]
  [HasabLspLauncher]
}

package "Feature Engines" <<lsp>> {
  [DiagnosticEngine]
  [CompletionEngine]
  [HoverEngine]
  [DefinitionEngine]
  [ReferenceEngine]
  [RenameEngine]
  [SignatureEngine]
  [FormattingEngine]
  [CodeActionEngine]
  [DocumentHighlightEngine]
  [WorkspaceSymbolEngine]
}

package "Infrastructure" <<lsp>> {
  [DocumentState]
  [WorkspaceIndex]
  [LspLogger]
  [PerformanceMetrics]
}

package "HASAB Compiler Frontend" <<compiler>> {
  [Lexer]
  [Parser]
  [SemanticAnalyzer]
  [TypeChecker]
  [SymbolTable]
}

package "HASAB AST" <<core>> {
  [Module]
  [Decl]
  [Expr]
  [Stmt]
  [TypeNode]
}

[HasabLanguageServer] --> [HasabTextDocumentService]
[HasabLanguageServer] --> [HasabWorkspaceService]
[HasabLspLauncher] --> [HasabLanguageServer]

[HasabTextDocumentService] --> [DiagnosticEngine]
[HasabTextDocumentService] --> [CompletionEngine]
[HasabTextDocumentService] --> [HoverEngine]
[HasabTextDocumentService] --> [DefinitionEngine]
[HasabTextDocumentService] --> [ReferenceEngine]
[HasabTextDocumentService] --> [RenameEngine]
[HasabTextDocumentService] --> [SignatureEngine]
[HasabTextDocumentService] --> [FormattingEngine]
[HasabTextDocumentService] --> [CodeActionEngine]
[HasabTextDocumentService] --> [DocumentHighlightEngine]

[HasabWorkspaceService] --> [WorkspaceSymbolEngine]

[DiagnosticEngine] --> [DocumentState]
[CompletionEngine] --> [WorkspaceIndex]
[HoverEngine] --> [DocumentState]
[DefinitionEngine] --> [WorkspaceIndex]
[ReferenceEngine] --> [WorkspaceIndex]
[RenameEngine] --> [WorkspaceIndex]
[DocumentHighlightEngine] --> [DocumentState]
[WorkspaceSymbolEngine] --> [WorkspaceIndex]

[DocumentState] --> [Lexer]
[DocumentState] --> [Parser]
[DocumentState] --> [SemanticAnalyzer]
[DocumentState] --> [TypeChecker]
[DocumentState] --> [SymbolTable]

[WorkspaceIndex] --> [DocumentState]
[WorkspaceIndex] --> [Decl]

[FormattingEngine] ..> [HasabFormatter] : delegates

@enduml
```

---

## 8. LSP Request Flow — Sequence Diagram

```plantuml
@startuml HASAB_LSP_Request_Flow
skinparam sequenceMessageAlign center
title HASAB LSP Request Processing Flow

actor Editor
participant "LSP Client"
participant "HasabLanguageServer"
participant "TextDocumentService"
participant "Engine"
participant "DocumentState"
participant "WorkspaceIndex"
participant "Compiler"

Editor -> LSP Client : User action
activate LSP Client

LSP Client -> HasabLanguageServer : JSON-RPC request
activate HasabLanguageServer

HasabLanguageServer -> TextDocumentService : dispatch
activate TextDocumentService

TextDocumentService -> WorkspaceIndex : getDocument(uri)
activate WorkspaceIndex
WorkspaceIndex --> TextDocumentService : DocumentState
deactivate WorkspaceIndex

TextDocumentService -> DocumentState : get/parse content
activate DocumentState
DocumentState -> Compiler : Lexer -> Parser
activate Compiler
Compiler --> DocumentState : ParseResult + AST
deactivate Compiler

DocumentState -> Compiler : SemanticAnalyzer
activate Compiler
Compiler --> DocumentState : SemanticModel
deactivate Compiler

DocumentState -> Compiler : TypeChecker
activate Compiler
Compiler --> DocumentState : TypeCheckResult
deactivate Compiler

DocumentState --> TextDocumentService : FullAnalysisResult
deactivate DocumentState

TextDocumentService -> Engine : computeXxx()
activate Engine
Engine --> TextDocumentService : result
deactivate Engine

TextDocumentService --> HasabLanguageServer : LSP response
deactivate TextDocumentService

HasabLanguageServer --> LSP Client : JSON-RPC response
deactivate HasabLanguageServer

LSP Client --> Editor : Update UI
deactivate LSP Client

@enduml
```

---

## 9. Document Lifecycle — State Diagram

```plantuml
@startuml HASAB_Document_Lifecycle
title HASAB Document Lifecycle State Diagram

[*] --> Closed : editor opens file

Closed --> Open : didOpen
note right of Open
  Create DocumentState
  Initial parse + analysis
  Index symbols in WorkspaceIndex
  Publish diagnostics
end note

Open --> Parsing : content changed
Parsing --> Analyzing : parse complete
Analyzing --> Publishing : analysis complete
Publishing --> Open : diagnostics sent

Open --> Closed : didClose
note right of Closed
  Remove from WorkspaceIndex
  Clear diagnostics
end note

Open --> Saving : didSave
Saving --> Publishing : save complete

Parsing --> Open : parse failed (recoverable)

@enduml
```

---

## 10. LSP Server Class Diagram

```plantuml
@startuml HASAB_LSP_Class_Diagram
skinparam linetype ortho
skinparam classAttributeIconSize 0
title HASAB LSP Server — Core Class Diagram

package "hasab.lsp" {
  class HasabLanguageServer {
    -workspaceIndex: WorkspaceIndex
    -logger: LspLogger
    -metrics: PerformanceMetrics
    +initialize(params): InitializeResult
    +shutdown(): CompletableFuture
    +exit()
    +getTextDocumentService(): HasabTextDocumentService
    +getWorkspaceService(): HasabWorkspaceService
    +getWorkspaceIndex(): WorkspaceIndex
    +getPerformanceMetrics(): PerformanceMetrics
  }

  class HasabTextDocumentService {
    -diagnosticEngine: DiagnosticEngine
    -completionEngine: CompletionEngine
    -hoverEngine: HoverEngine
    -definitionEngine: DefinitionEngine
    -referenceEngine: ReferenceEngine
    -renameEngine: RenameEngine
    -signatureEngine: SignatureEngine
    -formattingEngine: FormattingEngine
    -codeActionEngine: CodeActionEngine
    -highlightEngine: DocumentHighlightEngine
    +client: LanguageClient?
    +didOpen(params)
    +didChange(params)
    +didClose(params)
    +didSave(params)
    +completion(params): CompletableFuture
    +hover(params): CompletableFuture
    +definition(params): CompletableFuture
    +references(params): CompletableFuture
    +documentHighlight(params): CompletableFuture
    +documentSymbol(params): CompletableFuture
    +signatureHelp(params): CompletableFuture
    +formatting(params): CompletableFuture
    +rangeFormatting(params): CompletableFuture
    +onTypeFormatting(params): CompletableFuture
    +codeAction(params): CompletableFuture
    +rename(params): CompletableFuture
  }

  class HasabWorkspaceService {
    -symbolEngine: WorkspaceSymbolEngine
    +didChangeWorkspaceFolders(params)
    +didChangeConfiguration(params)
    +didChangeWatchedFiles(params)
    +symbol(params): CompletableFuture
    +executeCommand(params): CompletableFuture
  }

  class DocumentState {
    +uri: String
    +languageId: String
    +version: Int
    +content: String
    +lexerResult: LexerResult?
    +parseResult: ParseResult?
    +semanticModel: SemanticModel?
    +typeCheckResult: TypeCheckResult?
    +updateContent(newContent, newVersion)
    +parse(): ParseResult
    +analyzeSemantics(): SemanticModel
    +typeCheck(): TypeCheckResult
    +fullAnalysis(): FullAnalysisResult
    +invalidate()
  }

  class WorkspaceIndex {
    -documents: Map
    -fileSymbols: Map
    -symbolUsages: Map
    +addDocument(state)
    +removeDocument(uri)
    +updateDocument(state)
    +getDocument(uri): DocumentState?
    +getAllDocuments(): Map
    +getSymbolsForFile(uri): List
    +getAllSymbols(): List
    +getSymbolsByName(name): List
    +getSymbolsByKind(kind): List
    +searchSymbols(query): List
    +findUsages(name): List
  }
}

package "hasab.lsp.logging" {
  class LspLogger {
    -prefix: String
    -minLevel: Level
    +debug(message, throwable)
    +info(message, throwable)
    +warn(message, throwable)
    +error(message, throwable)
    +getRecentLogs(count): List
    +getLogsByLevel(level): List
    +clear()
  }

  class PerformanceMetrics {
    -counters: Map
    -timings: Map
    +incrementCounter(name)
    +recordTiming(name, durationNs)
    +measure(name, block): T
    +getCounter(name): Long
    +getAverageTimingNs(name): Double
    +snapshot(): Map
    +reset()
  }
}

HasabLanguageServer *--> HasabTextDocumentService
HasabLanguageServer *--> HasabWorkspaceService
HasabLanguageServer *--> WorkspaceIndex
HasabLanguageServer *--> PerformanceMetrics

HasabTextDocumentService o--> DiagnosticEngine
HasabTextDocumentService o--> CompletionEngine
HasabTextDocumentService o--> HoverEngine
HasabTextDocumentService o--> DefinitionEngine
HasabTextDocumentService o--> ReferenceEngine
HasabTextDocumentService o--> RenameEngine
HasabTextDocumentService o--> SignatureEngine
HasabTextDocumentService o--> FormattingEngine
HasabTextDocumentService o--> CodeActionEngine
HasabTextDocumentService o--> DocumentHighlightEngine

HasabWorkspaceService o--> WorkspaceSymbolEngine

WorkspaceIndex o--> DocumentState
DocumentState --> WorkspaceIndex : indexed by

@enduml
```

---

## 11. Feature Engine Architecture — Package Diagram

```plantuml
@startuml HASAB_LSP_Package_Diagram
title HASAB LSP Feature Engine Package Structure

package "hasab.lsp" {
  [HasabLanguageServer]
  [HasabTextDocumentService]
  [HasabWorkspaceService]
  [DocumentState]
  [WorkspaceIndex]
}

package "hasab.lsp.diagnostics" {
  [DiagnosticEngine]
}

package "hasab.lsp.completion" {
  [CompletionEngine]
}

package "hasab.lsp.hover" {
  [HoverEngine]
}

package "hasab.lsp.definition" {
  [DefinitionEngine]
}

package "hasab.lsp.references" {
  [ReferenceEngine]
}

package "hasab.lsp.rename" {
  [RenameEngine]
}

package "hasab.lsp.signature" {
  [SignatureEngine]
}

package "hasab.lsp.formatting" {
  [FormattingEngine]
}

package "hasab.lsp.codeaction" {
  [CodeActionEngine]
}

package "hasab.lsp.highlighting" {
  [DocumentHighlightEngine]
}

package "hasab.lsp.symbol" {
  [WorkspaceSymbolEngine]
}

package "hasab.lsp.logging" {
  [LspLogger]
  [PerformanceMetrics]
}

[HasabTextDocumentService] --> [DiagnosticEngine]
[HasabTextDocumentService] --> [CompletionEngine]
[HasabTextDocumentService] --> [HoverEngine]
[HasabTextDocumentService] --> [DefinitionEngine]
[HasabTextDocumentService] --> [ReferenceEngine]
[HasabTextDocumentService] --> [RenameEngine]
[HasabTextDocumentService] --> [SignatureEngine]
[HasabTextDocumentService] --> [FormattingEngine]
[HasabTextDocumentService] --> [CodeActionEngine]
[HasabTextDocumentService] --> [DocumentHighlightEngine]
[HasabWorkspaceService] --> [WorkspaceSymbolEngine]

@enduml
```

---

## 12. State Diagram — Incremental Change Tracking

```plantuml
@startuml IncrementalChangeTracking
skinparam state {
  BackgroundColor LightBlue
  BorderColor DarkBlue
  ArrowColor DarkBlue
}
title Document Incremental Change Tracking

state "Idle" as idle
state "Content Updated" as updated
state "Classify Change" as classify
state "Non-Semantic" as nonsem
state "Skip Re-analysis" as skip
state "Semantic Change" as sem
state "Invalidate All Caches" as invalidate
state "Structural Change" as structural
state "Full Invalidation + Re-index" as full

idle --> updated : didChange()
updated --> classify : detectChangeType()
classify --> nonsem : only whitespace/comments changed
nonsem --> skip : return early
skip --> idle : content updated
classify --> sem : code changed (no brace diff)
sem --> invalidate : invalidate()
invalidate --> idle : re-analyze on next request
classify --> structural : braces/brackets changed
structural --> full : invalidate() + updateDocument()
full --> idle : full re-analysis on next request

@enduml
```
