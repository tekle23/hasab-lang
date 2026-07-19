# HASAB Language Support for VS Code

Provides language support for the HASAB programming language in Visual Studio Code.

## Features

- Syntax highlighting
- Auto-completion
- Hover information
- Go to definition
- Find references
- Rename symbol
- Signature help
- Code formatting
- Code actions (quick fixes)
- Diagnostics (errors and warnings)
- Document symbols
- Workspace symbol search

## Requirements

- Java 21 or later
- HASAB language server JAR (built from the project)

## Installation

### From Source

1. Build the language server JAR:
   ```bash
   ./gradlew shadowJar
   ```

2. Open this directory in VS Code:
   ```bash
   code editors/vscode
   ```

3. Press F5 to launch the Extension Development Host.

### Configuration

Add to your `settings.json`:

```json
{
  "hasab.server.path": "/path/to/hasab-lang-1.0.0-all.jar",
  "hasab.trace.server": "verbose"
}
```

## Commands

- `HASAB: Restart Language Server` - Restart the language server
- `HASAB: Show Output` - Show the language server output channel

## File Extensions

The extension activates for files with `.hs` and `.hasab` extensions.
