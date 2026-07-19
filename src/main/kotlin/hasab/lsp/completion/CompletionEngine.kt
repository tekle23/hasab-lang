package hasab.lsp.completion

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.Block
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.IdentifierExpr
import hasab.compiler.frontend.ast.IdentifierType
import hasab.compiler.frontend.ast.FieldAccessExpr
import hasab.compiler.frontend.ast.CallExpr
import hasab.compiler.frontend.ast.LetStmt
import hasab.compiler.frontend.ast.Module
import hasab.compiler.frontend.lexer.Keyword
import hasab.compiler.frontend.lexer.TokenType
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.SymbolKind
import hasab.compiler.semantic.SymbolTable
import hasab.lsp.DocumentState
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

public class CompletionEngine(
    private val workspaceIndex: WorkspaceIndex,
) {

    public fun computeCompletions(
        state: DocumentState,
        position: Position,
    ): List<CompletionItem> {
        val result = mutableListOf<CompletionItem>()

        addKeywordCompletions(result, state, position)
        addSymbolCompletions(result, state, position)
        addSnippetCompletions(result, state, position)
        addFieldCompletions(result, state, position)
        addBuiltInFunctionCompletions(result, state, position)

        return result
    }

    private fun addKeywordCompletions(result: MutableList<CompletionItem>, state: DocumentState, position: Position) {
        val keywords = listOf(
            KeywordCompletion("fn", "Function declaration", "fn ${"$"}1(${"$"}2): ${"$"}3 {\n\t${"$"}0\n}", "decl-fn"),
            KeywordCompletion("let", "Variable binding", "let ${"$"}1 = ${"$"}2", "decl-let"),
            KeywordCompletion("mut", "Mutable variable", "let mut ${"$"}1 = ${"$"}2", "decl-mut"),
            KeywordCompletion("if", "Conditional", "if ${"$"}1 {\n\t${"$"}0\n}", "ctrl-if"),
            KeywordCompletion("else", "Alternative branch", "else {\n\t${"$"}0\n}", "ctrl-else"),
            KeywordCompletion("while", "While loop", "while ${"$"}1 {\n\t${"$"}0\n}", "ctrl-while"),
            KeywordCompletion("for", "For loop", "for ${"$"}1 in ${"$"}2 {\n\t${"$"}0\n}", "ctrl-for"),
            KeywordCompletion("return", "Return value", "return ${"$"}1", "ctrl-return"),
            KeywordCompletion("break", "Break loop", "break", "ctrl-break"),
            KeywordCompletion("continue", "Continue loop", "continue", "ctrl-continue"),
            KeywordCompletion("struct", "Struct declaration", "struct ${"$"}1 {\n\t${"$"}0\n}", "decl-struct"),
            KeywordCompletion("enum", "Enum declaration", "enum ${"$"}1 {\n\t${"$"}0\n}", "decl-enum"),
            KeywordCompletion("impl", "Implementation block", "impl ${"$"}1 {\n\t${"$"}0\n}", "decl-impl"),
            KeywordCompletion("trait", "Trait declaration", "trait ${"$"}1 {\n\t${"$"}0\n}", "decl-trait"),
            KeywordCompletion("pub", "Public visibility", "pub ${"$"}1", "modifier-pub"),
            KeywordCompletion("mod", "Module declaration", "mod ${"$"}1 {\n\t${"$"}0\n}", "decl-mod"),
            KeywordCompletion("use", "Import", "use ${"$"}1", "decl-use"),
            KeywordCompletion("type", "Type alias", "type ${"$"}1 = ${"$"}2", "decl-type"),
            KeywordCompletion("new", "Constructor call", "new ${"$"}1(${"$"}2)", "expr-new"),
            KeywordCompletion("this", "Self reference", "this", "expr-this"),
            KeywordCompletion("super", "Super reference", "super", "expr-super"),
            KeywordCompletion("true", "Boolean true", "true", "lit-true"),
            KeywordCompletion("false", "Boolean false", "false", "lit-false"),
            KeywordCompletion("nil", "Null literal", "nil", "lit-nil"),
            KeywordCompletion("void", "Void type", "void", "type-void"),
            KeywordCompletion("int", "Integer type", "int", "type-int"),
            KeywordCompletion("float", "Float type", "float", "type-float"),
            KeywordCompletion("string", "String type", "string", "type-string"),
            KeywordCompletion("bool", "Boolean type", "bool", "type-bool"),
            KeywordCompletion("char", "Char type", "char", "type-char"),
        )

        for (kw in keywords) {
            result.add(
                CompletionItem().apply {
                    label = kw.label
                    detail = kw.description
                    kind = CompletionItemKind.Keyword
                    insertText = kw.snippet
                    insertTextFormat = InsertTextFormat.Snippet
                    sortText = "1_${kw.label}"
                    filterText = kw.label
                }
            )
        }

        for (builtin in listOf("print", "println", "len", "typeof")) {
            result.add(
                CompletionItem().apply {
                    label = builtin
                    detail = "Built-in function"
                    kind = CompletionItemKind.Function
                    sortText = "2_$builtin"
                }
            )
        }
    }

    private fun addSymbolCompletions(result: MutableList<CompletionItem>, state: DocumentState, position: Position) {
        try {
            val semanticModel = state.semanticModel ?: state.analyzeSemantics()
            val symbolTable = semanticModel.symbolTable

            val allSymbols = symbolTable.allVisibleSymbols()
            for ((name, symbol) in allSymbols) {
                val itemKind = when (symbol.kind) {
                    SymbolKind.FUNCTION -> CompletionItemKind.Function
                    SymbolKind.VARIABLE -> CompletionItemKind.Variable
                    SymbolKind.STRUCT -> CompletionItemKind.Struct
                    SymbolKind.ENUM -> CompletionItemKind.Enum
                    SymbolKind.TRAIT -> CompletionItemKind.Interface
                    SymbolKind.TYPE_ALIAS -> CompletionItemKind.TypeParameter
                    SymbolKind.MODULE -> CompletionItemKind.Module
                    SymbolKind.PARAMETER -> CompletionItemKind.Variable
                    SymbolKind.FIELD -> CompletionItemKind.Field
                    SymbolKind.VARIANT -> CompletionItemKind.EnumMember
                }

                val doc = symbol.docComment?.let { org.eclipse.lsp4j.MarkupContent("markdown", it) }

                result.add(
                    CompletionItem().apply {
                        label = name
                        kind = itemKind
                        detail = symbol.kind.name.lowercase()
                        documentation = doc?.let { org.eclipse.lsp4j.jsonrpc.messages.Either.forRight(it) }
                        sortText = "3_$name"
                    }
                )
            }

            val fileSymbols = workspaceIndex.getSymbolsForFile(state.uri)
            for (sym in fileSymbols) {
                if (allSymbols.containsKey(sym.name)) continue
                result.add(
                    CompletionItem().apply {
                        label = sym.name
                        kind = when (sym.kind) {
                            SymbolKind.FUNCTION -> CompletionItemKind.Function
                            SymbolKind.VARIABLE -> CompletionItemKind.Variable
                            SymbolKind.STRUCT -> CompletionItemKind.Struct
                            SymbolKind.ENUM -> CompletionItemKind.Enum
                            SymbolKind.TRAIT -> CompletionItemKind.Interface
                            SymbolKind.TYPE_ALIAS -> CompletionItemKind.TypeParameter
                            SymbolKind.MODULE -> CompletionItemKind.Module
                            SymbolKind.PARAMETER -> CompletionItemKind.Variable
                            SymbolKind.FIELD -> CompletionItemKind.Field
                            SymbolKind.VARIANT -> CompletionItemKind.EnumMember
                        }
                        detail = "file symbol"
                        sortText = "4_${sym.name}"
                    }
                )
            }
        } catch (_: Exception) {
            // Semantic analysis failed, fall back to workspace index
            val fileSymbols = workspaceIndex.getSymbolsForFile(state.uri)
            for (sym in fileSymbols) {
                result.add(
                    CompletionItem().apply {
                        label = sym.name
                        kind = when (sym.kind) {
                            SymbolKind.FUNCTION -> CompletionItemKind.Function
                            SymbolKind.VARIABLE -> CompletionItemKind.Variable
                            SymbolKind.STRUCT -> CompletionItemKind.Struct
                            SymbolKind.ENUM -> CompletionItemKind.Enum
                            SymbolKind.TRAIT -> CompletionItemKind.Interface
                            SymbolKind.TYPE_ALIAS -> CompletionItemKind.TypeParameter
                            SymbolKind.MODULE -> CompletionItemKind.Module
                            SymbolKind.PARAMETER -> CompletionItemKind.Variable
                            SymbolKind.FIELD -> CompletionItemKind.Field
                            SymbolKind.VARIANT -> CompletionItemKind.EnumMember
                        }
                        sortText = "5_${sym.name}"
                    }
                )
            }
        }
    }

    private fun addSnippetCompletions(result: MutableList<CompletionItem>, state: DocumentState, position: Position) {
        val snippets = listOf(
            SnippetCompletion("fn-main", "Main function", "fn main() {\n\t${"$"}0\n}", "Snippet: main function"),
            SnippetCompletion("fn-pub", "Public function", "pub fn ${"$"}1(${"$"}2): ${"$"}3 {\n\t${"$"}0\n}", "Snippet: public function"),
            SnippetCompletion("struct", "Struct", "struct ${"$"}1 {\n\t${"$"}2: ${"$"}3\n}", "Snippet: struct"),
            SnippetCompletion("enum", "Enum", "enum ${"$"}1 {\n\t${"$"}2(${"$"}3)\n}", "Snippet: enum"),
            SnippetCompletion("if-else", "If-else", "if ${"$"}1 {\n\t${"$"}2\n} else {\n\t${"$"}0\n}", "Snippet: if-else"),
            SnippetCompletion("for-in", "For-in loop", "for ${"$"}1 in ${"$"}2 {\n\t${"$"}0\n}", "Snippet: for-in loop"),
            SnippetCompletion("while", "While loop", "while ${"$"}1 {\n\t${"$"}0\n}", "Snippet: while loop"),
            SnippetCompletion("impl", "Impl block", "impl ${"$"}1 {\n\tfn ${"$"}2(${"$"}3): ${"$"}4 {\n\t\t${"$"}0\n\t}\n}", "Snippet: impl block"),
        )

        for (snippet in snippets) {
            result.add(
                CompletionItem().apply {
                    label = snippet.label
                    detail = snippet.description
                    kind = CompletionItemKind.Snippet
                    insertText = snippet.snippet
                    insertTextFormat = InsertTextFormat.Snippet
                    documentation = org.eclipse.lsp4j.jsonrpc.messages.Either.forRight(org.eclipse.lsp4j.MarkupContent("markdown", snippet.description))
                    sortText = "0_${snippet.label}"
                }
            )
        }
    }

    private fun addFieldCompletions(result: MutableList<CompletionItem>, state: DocumentState, position: Position) {
        try {
            val parsed = state.parseResult ?: state.parse()
            val module = parsed.module

            val content = state.content
            val offset = positionToOffset(content, position)
            if (offset < 0 || offset >= content.length) return

            val dotPrefix = findDotPrefix(content, offset)
            if (dotPrefix != null) {
                val semanticModel = state.semanticModel ?: state.analyzeSemantics()
                val symbol = semanticModel.lookupSymbol(dotPrefix)
                if (symbol != null) {
                    when (symbol.kind) {
                        SymbolKind.STRUCT, SymbolKind.ENUM -> {
                            result.add(
                                CompletionItem().apply {
                                    label = dotPrefix
                                    kind = CompletionItemKind.Field
                                    detail = "struct field"
                                    sortText = "6_$dotPrefix"
                                }
                            )
                        }
                        else -> {}
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun addBuiltInFunctionCompletions(result: MutableList<CompletionItem>, state: DocumentState, position: Position) {
        val builtins = listOf(
            Pair("print", "(value: any): void"),
            Pair("println", "(value: any): void"),
            Pair("len", "(collection: any): int"),
            Pair("typeof", "(value: any): string"),
            Pair("assert", "(condition: bool): void"),
            Pair("to_string", "(value: any): string"),
            Pair("to_int", "(value: any): int"),
            Pair("to_float", "(value: any): float"),
        )

        for ((name, signature) in builtins) {
            result.add(
                CompletionItem().apply {
                    label = name
                    detail = "Built-in$signature"
                    kind = CompletionItemKind.Function
                    insertText = name
                    sortText = "2_$name"
                }
            )
        }
    }

    private fun positionToOffset(content: String, position: Position): Int {
        var offset = 0
        var line = 0
        var col = 0
        for (ch in content) {
            if (line == position.line && col == position.character) return offset
            if (ch == '\n') {
                line++
                col = 0
            } else {
                col++
            }
            offset++
        }
        return if (line == position.line && col == position.character) offset else -1
    }

    private fun findDotPrefix(content: String, offset: Int): String? {
        if (offset <= 0 || content[offset - 1] != '.') return null
        var i = offset - 2
        while (i >= 0 && (content[i].isLetterOrDigit() || content[i] == '_')) {
            i--
        }
        return content.substring(i + 1, offset - 1).ifEmpty { null }
    }

    private data class KeywordCompletion(
        val label: String,
        val description: String,
        val snippet: String,
        val sortKey: String,
    )

    private data class SnippetCompletion(
        val label: String,
        val description: String,
        val snippet: String,
        val documentation: String,
    )
}
