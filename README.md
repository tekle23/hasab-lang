# HASAB Programming Language - Lexer v1.0

A production-quality lexical analyzer for the HASAB programming language, implemented in Kotlin 2.x.

## Overview

The HASAB lexer transforms raw source code into a stream of tokens. It supports **UTF-8 full Unicode** including **Ethiopic (Amharic) characters**, enabling identifiers and keywords in both Latin and Amharic scripts.

## Architecture

```
hasab.lang.lexer/
├── SourcePosition.kt    # Line, column, offset in source
├── SourceRange.kt       # Start-to-end span in source
├── SourceFile.kt        # Source file abstraction (name + content)
├── TokenType.kt         # Sealed class hierarchy of all token types
├── Token.kt             # Token data class with full position info
├── Keyword.kt           # Latin/Amharic keyword registry with bidirectional lookup
├── LexerConfig.kt       # Configurable lexer settings
├── LexerResult.kt       # Result wrapper (tokens + diagnostics)
├── Diagnostic.kt        # Error/warning reporting with source ranges
└── Lexer.kt             # Core scanning engine
```

### Class Diagram

```
┌─────────────────┐     ┌──────────────────┐
│   SourceFile     │     │   LexerConfig     │
│─────────────────│     │──────────────────│
│ name: String    │     │ maxStringLen: Int │
│ content: String │     │ allowUnicode: Bool│
│ charAt()        │     │ commentPrefix     │
└────────┬────────┘     └────────┬─────────┘
         │                       │
         ▼                       ▼
    ┌─────────────────────────────────┐
    │            Lexer                │
    │─────────────────────────────────│
    │ source: SourceFile              │
    │ config: LexerConfig             │
    │ pos, line, column: Int          │
    │─────────────────────────────────│
    │ tokenize(): LexerResult         │
    │ nextToken(): Token              │
    │ scanString/Number/Char/Ident()  │
    │ scanOperatorOrDelimiter()       │
    │ skipWhitespaceAndComments()     │
    └───────────────┬─────────────────┘
                    │ produces
                    ▼
    ┌─────────────────────────────────┐
    │         LexerResult             │
    │─────────────────────────────────│
    │ tokens: List<Token>             │
    │ diagnostics: List<Diagnostic>   │
    │ hasErrors: Boolean              │
    └─────────────────────────────────┘

    ┌─────────────────┐     ┌──────────────────────┐
    │    Token         │     │     TokenType         │
    │─────────────────│     │──────────────────────│
    │ type: TokenType │     │ (sealed class)        │
    │ lexeme: String  │     │ IntegerLiteral        │
    │ fileName: String│     │ FloatLiteral          │
    │ line: Int       │     │ StringLiteral         │
    │ column: Int     │     │ CharacterLiteral      │
    │ startOffset: Int│     │ Identifier            │
    │ endOffset: Int  │     │ Keyword(keyword)      │
    └─────────────────┘     │ Plus, Minus, Star ... │
                            │ LeftParen, RightParen │
                            │ Eof                   │
                            └──────────────────────┘

    ┌──────────────────┐     ┌──────────────────┐
    │   Diagnostic      │     │     Keyword        │
    │──────────────────│     │──────────────────│
    │ severity         │     │ latin: String     │
    │ message          │     │ amharic: String   │
    │ range: SrcRange  │     │ tokenString: Str  │
    │ fileName         │     │ ALL: List<Kw>     │
    │ hint: String?    │     │ lookup(): Kw?     │
    └──────────────────┘     └──────────────────┘
```

## Token Types

| Category | Tokens |
|---|---|
| **Literals** | `IntegerLiteral`, `FloatLiteral`, `StringLiteral`, `CharacterLiteral` |
| **Identifiers** | `Identifier`, `Keyword(latin/amharic)` |
| **Arithmetic** | `+` `-` `*` `/` `%` |
| **Comparison** | `==` `!=` `<` `>` `<=` `>=` |
| **Logical** | `&&` `\|\|` `!` |
| **Bitwise** | `&` `\|` `^` `~` `<<` `>>` |
| **Assignment** | `=` `+=` `-=` `*=` `/=` `%=` |
| **Arrow/Path** | `->` `=>` `::` `.` `..` `..=` |
| **Delimiters** | `(` `)` `{` `}` `[` `]` `;` `,` `:` `_` |
| **Special** | `Eof` |

## Keyword System

Each keyword has **three names**: Latin, Amharic, and the canonical token string.

| Latin | Amharic (Ethiopic) | Token |
|---|---|---|
| `fn` | `ተግባር` | `fn` |
| `let` | `ለ` | `let` |
| `mut` | `ለተቀይညት` | `mut` |
| `if` | `ከመ` | `if` |
| `else` | `አይደለ` | `else` |
| `while` | `በ.repeat` | `while` |
| `return` | `ተመለስ` | `return` |
| `true` | `እውነት` | `true` |
| `false` | `ሐሰት` | `false` |
| `nil` | `ባዶ` | `nil` |
| `struct` | `ሰንstructor` | `struct` |
| `enum` | `ማሰሪያ` | `enum` |
| `pub` | `ተጨማሪ` | `pub` |
| ... | ... | ... |

Either script can be used in source code — both resolve to the same token.

## Usage

```kotlin
import hasab.lang.lexer.*

val source = SourceFile("main.hasab", """
    fn add(x, y) {
        return x + y;
    }

    let result = add(10, 20);
""".trimIndent())

val lexer = Lexer(source)
val result = lexer.tokenize()

for (token in result.tokens) {
    println(token)
}

if (result.hasErrors) {
    for (error in result.errors) {
        System.err.println(error)
    }
}
```

## Diagnostics

The lexer produces rich diagnostics with:

- **Severity**: `WARNING` or `ERROR`
- **Message**: Human-readable description
- **Source range**: Exact file location
- **Hint**: Suggested fix (optional)

Example diagnostics:
```
test.hasab:1:5: error: Unterminated string literal
  hint: Close the string with a double quote: "
test.hasab:2:1: warning: Unknown escape sequence: \z
test.hasab:3:1: error: Unexpected character: @
  hint: Remove this character or replace it with a valid token
```

## Building & Testing

```bash
# Compile
./gradlew compileKotlin

# Run all tests (69 tests)
./gradlew test

# Run a specific test
./gradlew test --tests "hasab.lang.lexer.LexerTest.simple identifier"
```

## Test Coverage

The test suite covers:

- Identifiers (Latin, Ethiopic, mixed scripts, underscores, digits)
- All 35 Latin keywords and Amharic keyword equivalents
- Integer and float literals (including scientific notation)
- String literals (escape sequences, unterminated strings)
- Character literals (escape sequences, unterminated chars)
- All operators (single-char, two-char, three-char)
- All delimiters
- Single-line and nested multi-line comments
- Whitespace handling (spaces, tabs, CRLF)
- Line/column/offset tracking across multiline sources
- Diagnostic generation for errors and warnings
- Edge cases (consecutive operators, dot vs range, paths)

## Performance Considerations

- **Single-pass**: The lexer scans the input in a single pass with O(n) complexity
- **No backtracking**: Each character is examined at most twice
- **CharArray access**: Uses direct `String` indexing for fast character access
- **Lazy keyword map**: The keyword lookup table is built once and cached via `lazy`
- **UTF-8 native**: Leverages Kotlin/JVM's native UTF-16 representation for Unicode

## License

HASAB Language Project
