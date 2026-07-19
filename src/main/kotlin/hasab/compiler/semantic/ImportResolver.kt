package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Resolves use/import declarations against known modules.
 * Reports unresolved imports and circular dependencies.
 */
public class ImportResolver(
    private val table: SymbolTable,
) {
    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()
    private val resolvedImports: MutableMap<String, ResolvedImport> = mutableMapOf()
    private val knownModules: MutableSet<String> = mutableSetOf()

    public fun collectModuleNames(module: Module) {
        knownModules.add(module.name ?: module.fileName)
        for (decl in module.declarations) {
            when (decl) {
                is ModDecl -> knownModules.add(decl.name)
                is PubDecl -> {
                    if (decl.inner is ModDecl) knownModules.add(decl.inner.name)
                }
                else -> {}
            }
        }
    }

    public fun resolve(module: Module): List<SemanticDiagnostic> {
        diagnostics.clear()
        resolveDeclarations(module.declarations)
        return diagnostics.toList()
    }

    private fun report(code: DiagnosticCode, message: String, node: AstNode, hint: String? = null) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = node.range(),
            fileName = node.fileName,
            hint = hint,
        ))
    }

    private fun resolveDeclarations(decls: List<Decl>) {
        for (decl in decls) {
            when (decl) {
                is UseDecl -> resolveUseDecl(decl)
                is ModDecl -> decl.body?.let { resolveDeclarations(it) }
                is PubDecl -> {
                    if (decl.inner is UseDecl) resolveUseDecl(decl.inner)
                    else if (decl.inner is ModDecl) decl.inner.body?.let { resolveDeclarations(it) }
                }
                else -> {}
            }
        }
    }

    private fun resolveUseDecl(node: UseDecl) {
        val fullPath = node.path.joinToString("::")
        val moduleName = node.path.firstOrNull() ?: ""

        // Check if the root module exists
        val rootSymbol = table.lookup(moduleName)
        if (rootSymbol == null && moduleName !in knownModules) {
            report(
                DiagnosticCode.UNRESOLVED_IMPORT,
                "Unresolved import '$fullPath'",
                node,
                hint = "No module named '$moduleName' has been declared",
            )
            return
        }

        resolvedImports[fullPath] = ResolvedImport(
            path = node.path,
            moduleName = moduleName,
            isWildcard = false,
            range = node.range(),
            fileName = node.fileName,
        )
    }

    /**
     * Get all resolved imports.
     */
    public fun resolvedImports(): Map<String, ResolvedImport> = resolvedImports.toMap()

    /**
     * Check for circular dependencies in the module graph.
     */
    public fun checkCircularDeps(moduleGraph: Map<String, ModuleInfo>): List<SemanticDiagnostic> {
        val result = mutableListOf<SemanticDiagnostic>()
        val visited = mutableSetOf<String>()
        val inStack = mutableSetOf<String>()

        fun dfs(moduleName: String) {
            if (moduleName in inStack) {
                result.add(SemanticDiagnostic(
                    code = DiagnosticCode.CIRCULAR_MODULE_DEPENDENCY,
                    severity = DiagnosticSeverity.ERROR,
                    message = "Circular dependency detected involving module '$moduleName'",
                    range = SourceRange(SourcePosition(1, 1, 0), SourcePosition(1, 1, 0)),
                    fileName = moduleGraph[moduleName]?.fileName ?: "",
                ))
                return
            }
            if (moduleName in visited) return

            visited.add(moduleName)
            inStack.add(moduleName)

            moduleGraph[moduleName]?.imports?.forEach { dep ->
                dfs(dep)
            }

            inStack.remove(moduleName)
        }

        for (moduleName in moduleGraph.keys) {
            dfs(moduleName)
        }

        return result
    }
}
