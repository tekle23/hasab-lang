# HASAB v0.9 Release Notes

## Overview

HASAB v0.9 is the first feature-complete release of the HASAB programming language compiler. This release validates the full compiler pipeline through a comprehensive test suite of 27 example programs that compile, generate Java source, compile to JVM bytecode, and execute with correct output.

---

## What's New in v0.9

### Compiler Pipeline — Fully Validated

The end-to-end compiler pipeline is validated from source to execution:

1. **Lexer** — Tokenizes HASAB source into a token stream with error recovery
2. **Parser** — Produces a complete AST with error recovery for all declaration and expression types
3. **Semantic Analyzer** — Two-pass analysis collecting declarations and resolving references with 40 diagnostic codes
4. **Type Checker** — Full type inference, constraint solving, and validation with 27 diagnostic codes
5. **Code Generator** — Produces compilable Java source with proper type mapping
6. **Javac Integration** — Compiles generated Java to JVM bytecode
7. **Execution** — Programs run correctly on the JVM

### Golden Test Suite — 27 Programs Verified End-to-End

Every example program compiles through the full pipeline and executes with verified output:

| # | Program | Lines of Output |
|---|---------|----------------|
| 01 | Hello World | 4 |
| 02 | Variables | 7 |
| 03 | Arithmetic | 16 |
| 04 | Control Flow | 9 |
| 05 | Loops | 15 |
| 06 | Functions | 8 |
| 07 | Structs | 7 |
| 08 | Enums | 6 |
| 09 | Arrays | 15 |
| 10 | Type Aliases | 4 |
| 11 | Impl Blocks | 3 |
| 12 | Traits | 2 |
| 13 | Built-in Functions | 13 |
| 14 | Optionals | 3 |
| 15 | Strings | 6 |
| 16 | Nested Structs | 6 |
| 17 | Recursion | 14 |
| 18 | Sorting | 3 |
| 19 | Boolean Logic | 12 |
| 20 | Nested Loops | 12 |
| 20 | Ranges | 19 |
| 21 | Block Expressions | 4 |
| 21 | If Expressions | 4 |
| 22 | FizzBuzz | 31 |
| 23 | Matrices | 5 |
| 24 | Search | 5 |
| 25 | Advanced Control | 15 |

### New Standard Library Built-ins

Added 13 new built-in functions:

- **Type conversion:** `to_int()`, `to_float()`, `typeof()`
- **Assertion:** `assert()`
- **String manipulation:** `substring()`, `contains()`, `trim()`, `upper()`, `lower()`, `reverse()`, `replace()`, `split()`, `starts_with()`, `ends_with()`

### Diagnostic Improvements

- **67 stable diagnostic codes** (40 semantic + 27 type) with HSB prefix
- **Bilingual messages** — English and Amharic for all type diagnostics
- **Actionable suggestions** — Built-in suggestions for common errors
- **Type alias parsing fix** — `type UserId = int;` now parses correctly
- **For-loop variable registration** — Loop variables properly tracked in symbol table

### Code Generator Improvements

- **Typed array literals** — `[1, 2, 3]` generates `new int[]{1, 2, 3}` instead of `new Object[]{...}`
- **Struct constructor detection** — `Point(3, 4)` generates `new Point(3, 4)`
- **Enum generation** — Simple enums generate Java enums; data enums generate classes with static factory methods
- **Impl block support** — Methods compile as static methods with proper `self` handling
- **Range expressions** — `0..5` compiles to valid Java
- **Java main method** — `fn main()` generates `public static void main(String[] args)`
- **Primitive array `len()`** — Supports `int[]`, `double[]`, `char[]`, etc.
- **Method name sanitization** — Reserved Java keywords are properly escaped

---

## Language Features Supported

| Feature | Status |
|---------|--------|
| Functions (with recursion) | ✅ |
| Variables (immutable + mutable) | ✅ |
| Arithmetic, comparison, logical, bitwise operators | ✅ |
| Compound assignment | ✅ |
| If/else, while, for-each loops | ✅ |
| Break, continue, return | ✅ |
| Structs with constructors | ✅ |
| Enums (simple + data-carrying) | ✅ |
| Impl blocks with methods | ✅ |
| Trait declarations | ✅ |
| Arrays (literal, init, index, modify) | ✅ |
| Nested arrays (2D) | ✅ |
| Type aliases | ✅ |
| Optional types (nil) | ✅ |
| Safe navigation (?.) | ✅ |
| Null assertion (!!) | ✅ |
| 24 built-in functions | ✅ |
| Module system (mod/use/pub) | ✅ |
| Dual-script support (Latin + Amharic) | ✅ |

---

## Known Limitations

1. **String iteration in for-loops** — Not yet supported; strings cannot be iterated character-by-character
2. **Block expressions returning values** — Block expressions do not return values
3. **Zero-arg struct constructors** — Generated but not automatically available
4. **`mut` function parameters** — Parameters are always pass-by-value; mutation not propagated
5. **JVM backend** — Architecture designed but not yet integrated into the pipeline
6. **WASM / Native backends** — Not yet available
7. **Enum `toString()`** — Data-carrying enums display as Java object references

---

## Test Coverage

| Component | Tests |
|-----------|-------|
| Lexer | Comprehensive token coverage |
| Parser | Declarations, expressions, statements, types, error recovery |
| Semantic Analysis | Symbol table, scope resolution, 39+ test cases |
| Type System | Type checking, environment, components, 30+ test cases |
| Code Generation | Backend tests, source map, type mapper |
| JVM Backend | Class builder, method builder, type mapper, bytecode validation, debug info |
| HIR / CFG | Lowering, validation, CFG construction |
| Optimizer | Constant folding/propagation, copy propagation, DCE, branch simplification |
| Compiler Regression | Full pipeline on all 27 examples |
| Golden Execution | Compile + execute + output verification on all 27 examples |
| Feature Verification | 32 individual feature tests |
| **Total Compiler Tests** | **994** |

---

## Benchmark Summary

Run `CompilerBenchmarkTest` for detailed performance numbers. Key metrics:

- **Single-example full pipeline:** <50ms average
- **All 27 examples serial compilation:** <2s average
- **Memory usage:** <50MB for full suite

---

*HASAB v0.9 — Released July 2026*
