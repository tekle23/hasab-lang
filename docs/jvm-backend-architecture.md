# HASAB JVM Backend Architecture

## Overview

The JVM backend translates HASAB's optimized CFG HIR into JVM `.class` files using the ASM bytecode manipulation library.

## Pipeline

```
Source Code
  → Lexer → Parser → TypeChecker → AstToHirLowering → CfgBuilder
  → HirCfgModule
  → [Optimizer passes]
  → JvmBackend
    → BytecodeGenerator (per function)
      → JvmInstructionEmitter (per instruction)
        → MethodBuilder → ClassWriter → .class bytes
    → BytecodeValidator
  → .class files
```

## Component Diagram

| Component | Responsibility |
|-----------|---------------|
| `JvmBackend` | Top-level orchestrator, manages struct/enum/main class generation |
| `ClassBuilder` | Wraps ASM ClassWriter, manages fields and methods |
| `MethodBuilder` | Wraps ASM MethodVisitor, provides convenience APIs |
| `BytecodeGenerator` | Per-function code generation from CFG blocks |
| `JvmInstructionEmitter` | Per-instruction bytecode emission |
| `JvmTypeMapper` | HASAB type to JVM descriptor mapping |
| `LocalVariableManager` | Register to local variable slot allocation |
| `StackFrameAnalyzer` | JVM stack map frame type computation |
| `BytecodeValidator` | Post-generation bytecode verification |
| `DebugInfoBuilder` | Source line numbers and variable info |
| `ConstantPoolBuilder` | String constant deduplication |

## Type Mapping

| HASAB Type | JVM Descriptor | Slot Size |
|-----------|---------------|-----------|
| `int` | `I` | 1 |
| `float` | `F` | 1 |
| `bool` | `Z` | 1 |
| `char` | `C` | 1 |
| `string` | `Ljava/lang/String;` | 1 |
| `void` | `V` | 0 |
| `[T]` | `[T_desc` | 1 |
| `T?` | `Ljava/lang/Object;` | 1 |
| `*T` | `Ljava/lang/Object;` | 1 |

## Generated Classes

- **Structs** → Java classes with public fields and a parameterized constructor
- **Enums** → Java enum classes with static constants
- **Functions** → Static methods in the main class

## Built-in Function Mappings

| HASAB Function | JVM Equivalent |
|---------------|---------------|
| `println(s: string)` | `System.out.println(String)` |
| `println(n: int)` | `System.out.println(int)` |
| `print(s: string)` | `System.out.print(String)` |
| `len(s: string)` | `String.length()` |
| `len(a: [T])` | `arraylength` |

## Dependencies

- ASM 9.7.1 (`org.ow2.asm:asm`)
- ASM Util (`org.ow2.asm:asm-util`)
- ASM Tree (`org.ow2.asm:asm-tree`)

## Target JVM Version

- Java 21 (class file version 65)
- Uses `ClassWriter.COMPUTE_FRAMES` for automatic stack map frame generation
