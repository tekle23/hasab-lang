package hasab.lsp.symbol

import hasab.compiler.semantic.SymbolKind
import hasab.lsp.WorkspaceIndex
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolKind as LspSymbolKind

public class WorkspaceSymbolEngine(
    private val workspaceIndex: WorkspaceIndex,
) {

    public fun searchSymbols(query: String): List<SymbolInformation> {
        val symbols = workspaceIndex.searchSymbols(query)
        return symbols.map { sym ->
            SymbolInformation().apply {
                name = sym.name
                kind = mapSymbolKind(sym.kind)
                location = Location(
                    sym.uri,
                    Range(
                        Position(sym.range.start.line - 1, sym.range.start.column - 1),
                        Position(sym.range.end.line - 1, sym.range.end.column - 1),
                    )
                )
                if (sym.isPublic) {
                    containerName = "public"
                }
            }
        }
    }

    public fun getSymbolsByKind(filterKind: SymbolKind): List<SymbolInformation> {
        val symbols = workspaceIndex.getSymbolsByKind(filterKind)
        return symbols.map { sym ->
            SymbolInformation().apply {
                name = sym.name
                kind = mapSymbolKind(sym.kind)
                location = Location(
                    sym.uri,
                    Range(
                        Position(sym.range.start.line - 1, sym.range.start.column - 1),
                        Position(sym.range.end.line - 1, sym.range.end.column - 1),
                    )
                )
            }
        }
    }

    private fun mapSymbolKind(kind: SymbolKind): LspSymbolKind {
        return when (kind) {
            SymbolKind.FUNCTION -> LspSymbolKind.Function
            SymbolKind.VARIABLE -> LspSymbolKind.Variable
            SymbolKind.STRUCT -> LspSymbolKind.Struct
            SymbolKind.ENUM -> LspSymbolKind.Enum
            SymbolKind.TRAIT -> LspSymbolKind.Interface
            SymbolKind.TYPE_ALIAS -> LspSymbolKind.TypeParameter
            SymbolKind.MODULE -> LspSymbolKind.Module
            SymbolKind.PARAMETER -> LspSymbolKind.Variable
            SymbolKind.FIELD -> LspSymbolKind.Field
            SymbolKind.VARIANT -> LspSymbolKind.EnumMember
        }
    }
}
