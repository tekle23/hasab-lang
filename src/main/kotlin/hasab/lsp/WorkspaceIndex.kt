package hasab.lsp

import hasab.compiler.frontend.ast.AstNode
import hasab.compiler.frontend.ast.Decl
import hasab.compiler.frontend.ast.FnDecl
import hasab.compiler.frontend.ast.StructDecl
import hasab.compiler.frontend.ast.EnumDecl
import hasab.compiler.frontend.ast.TraitDecl
import hasab.compiler.frontend.ast.ImplDecl
import hasab.compiler.frontend.ast.TypeAliasDecl
import hasab.compiler.frontend.ast.ModDecl
import hasab.compiler.frontend.ast.UseDecl
import hasab.compiler.frontend.ast.AstVisitorBase
import hasab.compiler.frontend.ast.accept
import hasab.compiler.frontend.lexer.SourceRange
import hasab.compiler.semantic.SemanticModel
import hasab.compiler.semantic.Symbol
import hasab.compiler.semantic.SymbolKind
import hasab.compiler.semantic.Visibility
import java.util.concurrent.ConcurrentHashMap

public class WorkspaceIndex {

    private val documents = ConcurrentHashMap<String, DocumentState>()
    private val fileSymbols = ConcurrentHashMap<String, MutableList<IndexedSymbol>>()
    private val symbolUsages = ConcurrentHashMap<String, MutableList<UsageLocation>>()

    public fun addDocument(state: DocumentState) {
        documents[state.uri] = state
        indexDocument(state)
    }

    public fun removeDocument(uri: String) {
        val removed = documents.remove(uri)
        if (removed != null) {
            unindexDocument(uri)
        }
    }

    public fun updateDocument(state: DocumentState) {
        val old = documents[state.uri]
        if (old != null) {
            unindexDocument(state.uri)
        }
        documents[state.uri] = state
        indexDocument(state)
    }

    public fun getDocument(uri: String): DocumentState? = documents[uri]

    public fun getAllDocuments(): Map<String, DocumentState> = documents.toMap()

    public fun getSymbolsForFile(uri: String): List<IndexedSymbol> {
        return fileSymbols[uri]?.toList() ?: emptyList()
    }

    public fun getAllSymbols(): List<IndexedSymbol> {
        return fileSymbols.values.flatMap { it.toList() }
    }

    public fun getSymbolsByName(name: String): List<IndexedSymbol> {
        return getAllSymbols().filter { it.name == name }
    }

    public fun getSymbolsByKind(kind: SymbolKind): List<IndexedSymbol> {
        return getAllSymbols().filter { it.kind == kind }
    }

    public fun searchSymbols(query: String): List<IndexedSymbol> {
        val lowerQuery = query.lowercase()
        return getAllSymbols().filter { it.name.lowercase().contains(lowerQuery) }
    }

    public fun findUsages(name: String): List<UsageLocation> {
        return symbolUsages[name]?.toList() ?: emptyList()
    }

    public fun findUsagesInFile(name: String, uri: String): List<UsageLocation> {
        return findUsages(name).filter { it.uri == uri }
    }

    private fun indexDocument(state: DocumentState) {
        val uri = state.uri
        val symbols = mutableListOf<IndexedSymbol>()

        try {
            val parsed = state.parse()
            val module = parsed.module

            val indexer = SymbolIndexer(uri)
            for (decl in module.declarations) {
                symbols.addAll(indexer.indexDecl(decl))
            }
        } catch (_: Exception) {
            // Parsing failed, index what we can from semantic model
            val semantic = state.semanticModel
            if (semantic != null) {
                for ((node, sym) in semantic.nodeBindings) {
                    symbols.add(
                        IndexedSymbol(
                            name = sym.name,
                            kind = sym.kind,
                            range = sym.range,
                            uri = uri,
                            fileName = sym.fileName,
                            docComment = sym.docComment,
                            isPublic = sym.visibility == Visibility.PUBLIC,
                        )
                    )
                }
            }
        }

        if (symbols.isNotEmpty()) {
            fileSymbols[uri] = symbols
        }
    }

    private fun unindexDocument(uri: String) {
        fileSymbols.remove(uri)
        symbolUsages.entries.removeIf { entry ->
            entry.value.removeAll { it.uri == uri }
            entry.value.isEmpty()
        }
    }

    public data class IndexedSymbol(
        val name: String,
        val kind: SymbolKind,
        val range: SourceRange,
        val uri: String,
        val fileName: String,
        val docComment: String? = null,
        val isPublic: Boolean = false,
    )

    public data class UsageLocation(
        val uri: String,
        val fileName: String,
        val range: SourceRange,
    )

    private class SymbolIndexer(private val uri: String) : AstVisitorBase<List<IndexedSymbol>>(emptyList()) {

        fun indexDecl(decl: Decl): List<IndexedSymbol> = decl.accept(this)

        override fun visitFnDecl(node: FnDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            result.add(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.FUNCTION,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
            for (child in node.children()) {
                if (child is Decl) result.addAll(child.accept(this))
            }
            return result
        }

        override fun visitStructDecl(node: StructDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            result.add(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.STRUCT,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
            for (field in node.fields) {
                result.add(
                    IndexedSymbol(
                        name = field.name,
                        kind = SymbolKind.FIELD,
                        range = SourceRange(
                            hasab.compiler.frontend.lexer.SourcePosition(field.line, field.column, field.startOffset),
                            hasab.compiler.frontend.lexer.SourcePosition(field.line, field.column, field.endOffset),
                        ),
                        uri = uri,
                        fileName = field.fileName,
                        docComment = field.docComment,
                        isPublic = node.isPublic,
                    )
                )
            }
            return result
        }

        override fun visitEnumDecl(node: EnumDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            result.add(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.ENUM,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
            for (variant in node.variants) {
                result.add(
                    IndexedSymbol(
                        name = variant.name,
                        kind = SymbolKind.VARIANT,
                        range = SourceRange(
                            hasab.compiler.frontend.lexer.SourcePosition(variant.line, variant.column, variant.startOffset),
                            hasab.compiler.frontend.lexer.SourcePosition(variant.line, variant.column, variant.endOffset),
                        ),
                        uri = uri,
                        fileName = variant.fileName,
                        docComment = variant.docComment,
                        isPublic = node.isPublic,
                    )
                )
            }
            return result
        }

        override fun visitTraitDecl(node: TraitDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            result.add(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.TRAIT,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
            for (method in node.methods) {
                result.addAll(indexDecl(method))
            }
            return result
        }

        override fun visitImplDecl(node: ImplDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            for (method in node.methods) {
                result.addAll(indexDecl(method))
            }
            return result
        }

        override fun visitTypeAlias(node: TypeAliasDecl): List<IndexedSymbol> {
            return listOf(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.TYPE_ALIAS,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
        }

        override fun visitModDecl(node: ModDecl): List<IndexedSymbol> {
            val result = mutableListOf<IndexedSymbol>()
            result.add(
                IndexedSymbol(
                    name = node.name,
                    kind = SymbolKind.MODULE,
                    range = node.range(),
                    uri = uri,
                    fileName = node.fileName,
                    docComment = node.docComment,
                    isPublic = node.isPublic,
                )
            )
            for (decl in node.body.orEmpty()) {
                result.addAll(indexDecl(decl))
            }
            return result
        }

        override fun visitPubDecl(node: hasab.compiler.frontend.ast.PubDecl): List<IndexedSymbol> {
            return indexDecl(node.inner)
        }

        override fun visitUseDecl(node: UseDecl): List<IndexedSymbol> = emptyList()
    }
}
