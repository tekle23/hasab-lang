# Installing the HASAB CLI

HASAB is a standalone command-line tool. Once installed, it works independently of any source repository or build system — similar to `java`, `python`, `cargo`, `dotnet`, or `go`.

## Prerequisites

- **Java 21** or later (JDK or JRE)

Verify Java is installed:

```bash
java -version
```

## Install from Source

Clone the repository and build the distribution:

```bash
git clone https://github.com/hasab-lang/hasab-lang.git
cd hasab-lang
.\gradlew.bat dist      # Windows
./gradlew dist           # Linux/macOS
```

The distribution is assembled in `build/dist/`:

```
build/dist/
  hasab          # Linux/macOS launcher
  hasab.bat      # Windows launcher
  hasab.jar      # Self-contained executable JAR
```

### Add to PATH

**Windows:**

Add `build\dist` to your `PATH` environment variable, or copy the files to a directory already on your `PATH`.

**Linux/macOS:**

```bash
sudo cp build/dist/hasab build/dist/hasab.jar /usr/local/bin/
```

Or add to your shell profile:

```bash
export PATH="$PATH:/path/to/build/dist"
```

### Verify Installation

```bash
hasab --version
hasab doctor
```

## Quick Start

### 1. Create a new project

```bash
hasab new hello
cd hello
```

This creates:

```
hello/
  hasab.toml      # Project configuration
  src/             # Source files
    main.has
  tests/           # Test files
    main_test.has
  README.md
  .gitignore
```

### 2. Build the project

```bash
hasab build
```

Compiles all `.has` files through the full pipeline:
lex → parse → semantic analysis → type checking → Java code generation → javac → bytecode

### 3. Run the project

```bash
hasab run
```

Builds and executes the program. Output:

```
ሰላም ሃሳብ!
Welcome to HASAB!
```

### 4. Run tests

```bash
hasab test
```

## CLI Reference

```
Usage: hasab <command> [options]

Project Commands:
  new <name>       Create a new HASAB project
  build            Compile the current project
  run              Build and run the current project
  test             Run project tests
  clean            Remove build artifacts

Development Commands:
  fmt              Format source files
  lint             Lint source files for issues
  doc              Generate documentation

Package Commands:
  add <package>    Add a dependency
  remove <package> Remove a dependency
  publish          Publish to registry

Utility Commands:
  doctor           Check environment health
  version          Show version information
  help             Show this help message

Global Flags:
  --version, -v    Show version information
  --help, -h       Show help information
```

### Build Options

```bash
hasab build              # Debug build
hasab build --release    # Optimized release build
```

### Run Options

```bash
hasab run                        # Build and run
hasab run --args "arg1 arg2"     # Pass arguments to the program
hasab run --release              # Run with optimizations
```

### Test Options

```bash
hasab test                       # Run all tests
hasab test --filter "pattern"    # Run matching tests only
```

## Generated Project Structure

A project created with `hasab new` contains only:

| Path | Description |
|------|-------------|
| `hasab.toml` | Project configuration (name, version, source dirs) |
| `src/` | HASAB source files |
| `tests/` | HASAB test files |
| `README.md` | Project documentation |
| `.gitignore` | Git ignore rules |

The generated project is completely independent of the compiler source repository. It only requires the `hasab` CLI to be installed on your system.

## Project Configuration (hasab.toml)

```toml
[package]
name = "hello"
version = "0.1.0"
description = "A HASAB project"
repository = "https://packages.hasab.org"

[project]
source = "src"
tests = "tests"
entry = "main"
output = "build"
jvm_target = "21"
```

## Templates

```bash
hasab new myapp                      # Default template
hasab new myapp --template web       # Web application
hasab new myapp --template api       # API service
hasab new myapp --template library   # Library
hasab new myapp --template cli       # CLI application
hasab new myapp --template desktop   # Desktop application
```

## Troubleshooting

### "java is not recognized"

Install JDK 21+ and add it to your `PATH`. Verify with `java -version`.

### "hasab.jar not found"

Ensure the distribution files (`hasab`, `hasab.bat`, `hasab.jar`) are in the same directory and that directory is on your `PATH`.

### "hasab: command not found"

On Linux/macOS, ensure the launcher script is executable:

```bash
chmod +x hasab
```

Ensure the install directory is on your `PATH`.

---

*HASAB CLI Installation Guide*
