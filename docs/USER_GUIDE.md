# HASAB CLI User Guide

Complete reference for the HASAB developer toolchain.

---

## Table of Contents

1. [Installation](#installation)
2. [Quick Start](#quick-start)
3. [Project Commands](#project-commands)
4. [Dependency Management](#dependency-management)
5. [Code Quality](#code-quality)
6. [Documentation](#documentation)
7. [System Commands](#system-commands)
8. [Project Structure](#project-structure)
9. [Configuration Reference](#configuration-reference)
10. [Troubleshooting](#troubleshooting)

---

## Installation

HASAB requires Java 21+ and Gradle.

```bash
# Build from source
./gradlew.bat installDist   # Windows
./gradlew installDist       # Linux/macOS

# Or run directly
./gradlew.bat run --args="<command>"
```

---

## Quick Start

```bash
# Create a new project
hasab new my-app

# Enter the project
cd my-app

# Build it
hasab build

# Run it
hasab run

# Check code quality
hasab fmt
hasab lint
hasab doc
```

---

## Project Commands

### `hasab new <name>`

Scaffold a new HASAB project.

```bash
hasab new my-app              # Creates with standard template
hasab new my-app --template cli     # CLI application template
hasab new my-app --template lib     # Library template
```

**Generated structure:**
```
my-app/
  src/
    main.hs          # Main source file
  tests/
    main_test.hs     # Test file
  project.toml       # Project configuration
```

### `hasab build`

Compile all `.hs` source files to Java bytecode.

```bash
hasab build              # Standard build
hasab build --verbose    # Detailed output
```

**Output:** Compiled classes in `build/classes/`

### `hasab run`

Build and execute the project.

```bash
hasab run                # Build then run
hasab run --args "..."   # Pass arguments to program
```

### `hasab test`

Run all test files (`tests/` directory).

```bash
hasab test               # Run all tests
hasab test --verbose     # Detailed output
```

### `hasab clean`

Remove all build artifacts.

```bash
hasab clean              # Remove build/ directory
```

---

## Dependency Management

### `hasab add <package>[@version]`

Add a dependency to your project.

```bash
hasab add stdlib               # Latest version
hasab add stdlib@1.2.0         # Specific version
hasab add json-parser@2.0.0    # Named version
```

Updates `project.toml` automatically.

### `hasab remove <package>`

Remove a dependency.

```bash
hasab remove stdlib
hasab remove json-parser
```

### `hasab publish`

Publish your package to the registry (simulated).

```bash
hasab publish              # Uses name/version from project.toml
```

Requires `name` and `version` fields in `project.toml`.

---

## Code Quality

### `hasab fmt`

Format source files with consistent style.

```bash
hasab fmt                 # Format all .hs files
hasab fmt src/main.hs     # Format single file
```

**Formatting rules:**
- 2-space indentation
- Consistent spacing around operators
- Trailing whitespace removal
- Single newline at end of file

### `hasab lint`

Check source files for common issues.

```bash
hasab lint                # Lint all .hs files
hasab lint src/main.hs    # Lint single file
```

**Lint rules:**

| Rule | Severity | Description |
|------|----------|-------------|
| unused-variables | Warning | Variables declared but never used |
| empty-blocks | Warning | Empty function bodies or blocks |
| trailing-whitespace | Warning | Trailing spaces or tabs |
| long-lines | Info | Lines exceeding 120 characters |
| missing-documentation | Info | Public functions without doc comments |
| variable-shadowing | Warning | Inner variable names hiding outer ones |

### `hasab doc`

Generate API documentation from doc comments.

```bash
hasab doc                 # Generate docs/ directory
```

**Doc comment syntax:**
```hackus
/// Adds two numbers together
/// Takes an integer a and integer b
tetu add(a: kifil, b: kifil): kifil {
  yene a + b
}
```

Output: `docs/api.md` with formatted markdown documentation.

---

## System Commands

### `hasab doctor`

Check system requirements and diagnose issues.

```bash
hasab doctor
```

Checks for: Java, Gradle, HASAB_HOME, disk space, temp directory, etc.

### `hasab version`

Show version information.

```bash
hasab version
```

### `hasab help`

Show all available commands.

```bash
hasab help               # List all commands
hasab help build         # Show help for specific command
```

---

## Project Structure

A standard HASAB project:

```
my-app/
  src/
    main.hs              # Entry point source
    utils.hs             # Additional modules
  tests/
    main_test.hs         # Test files
  docs/
    api.md               # Generated documentation
  project.toml           # Project configuration
  build/
    classes/             # Compiled output
```

---

## Configuration Reference

`project.toml` is the project configuration file (TOML format):

```toml
name = "my-app"
version = "0.1.0"
description = "A HASAB application"
haskell_version = "0.1.0"

authors = ["Your Name"]
license = "MIT"
repository = "https://github.com/user/my-app"

dependencies = ["stdlib", "json-parser@2.0.0"]
```

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Project name |
| `version` | String | Yes | Semantic version |
| `description` | String | No | Short description |
| `haskell_version` | String | No | HASAB language version |
| `authors` | List | No | Author names |
| `license` | String | No | License identifier |
| `repository` | String | No | Source repository URL |
| `dependencies` | List | No | Package dependencies |

---

## Troubleshooting

### "Java not found" error

Ensure Java 21+ is installed and on your PATH:
```bash
java -version
```

### Build fails with compilation errors

Run with verbose output for details:
```bash
hasab build --verbose
```

### Tests not found

Ensure test files are in the `tests/` directory and end with `_test.hs`:
```
tests/
  my_test.hs      # Detected
  other.hs        # Not detected as test
```

### Formatter not changing anything

The formatter normalizes whitespace and indentation. If your code is already formatted, no changes are needed.

---

## HASAB Language Keywords

| Amharic | Latin | Meaning |
|---------|-------|---------|
| ተግባር | `fn` | Function declaration |
| ለ | `let` | Variable binding |
| ጻፍ | `print` | Print to output |
| መለስ | `return` | Return value |
| ከሆነ | `if` | Conditional |
| አይደለ | `else` | Alternative branch |
| ጠቅላላ | `pub` | Public visibility |
| `yene` | `yene` | Return keyword |
| `nna` | `nna` | If keyword |
| `sint` | `sint` | Else keyword |
| `kifil` | `int` | Integer type |
| `kifil` | `string` | String type |
| `hind` | `float` | Float type |

### Example Program

```hackus
// ጠቅላላ ተግባር (main function)
ተግባር main() {
  ጻፍ("Hello, ሐሳብ!")

  ለ x = 10
  ለ y = 20
  ጻፍ(x + y)

  ከሆነ x > 5 {
    ጻፍ("x is large")
  } አይደለ {
    ጻፍ("x is small")
  }
}
```
