package hasab.compiler.semantic

import hasab.compiler.frontend.ast.*
import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourcePosition
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Resolves the module/package hierarchy from mod declarations.
 * Builds a module graph and validates the structure.
 */
public class PackageResolver {

    private val diagnostics: MutableList<SemanticDiagnostic> = mutableListOf()
    private val moduleGraph: MutableMap<String, ModuleInfo> = mutableMapOf()

    public fun resolve(module: Module): List<SemanticDiagnostic> {
        diagnostics.clear()
        moduleGraph.clear()

        val moduleName = module.name ?: module.fileName
        val imports = collectImports(module.declarations)
        val declarations = collectDeclarationNames(module.declarations)

        moduleGraph[moduleName] = ModuleInfo(
            name = moduleName,
            fileName = module.fileName,
            declarations = declarations,
            imports = imports,
            isPublic = true,
        )

        resolveNestedModules(module.declarations, moduleName)
        return diagnostics.toList()
    }

    private fun report(code: DiagnosticCode, message: String, fileName: String, range: SourceRange) {
        diagnostics.add(SemanticDiagnostic(
            code = code,
            severity = DiagnosticSeverity.ERROR,
            message = message,
            range = range,
            fileName = fileName,
        ))
    }

    private fun collectImports(decls: List<Decl>): List<String> {
        val imports = mutableListOf<String>()
        for (decl in decls) {
            when (decl) {
                is UseDecl -> imports.add(decl.path.joinToString("::"))
                is ModDecl -> decl.body?.let { imports.addAll(collectImports(it)) }
                is PubDecl -> {
                    if (decl.inner is UseDecl) imports.add(decl.inner.path.joinToString("::"))
                    else if (decl.inner is ModDecl) decl.inner.body?.let { imports.addAll(collectImports(it)) }
                }
                else -> {}
            }
        }
        return imports
    }

    private fun collectDeclarationNames(decls: List<Decl>): List<String> {
        val names = mutableListOf<String>()
        for (decl in decls) {
            when (decl) {
                is FnDecl -> names.add(decl.name)
                is StructDecl -> names.add(decl.name)
                is EnumDecl -> names.add(decl.name)
                is TraitDecl -> names.add(decl.name)
                is TypeAliasDecl -> names.add(decl.name)
                is ImplDecl -> {} // impl blocks don't add new names
                is ModDecl -> names.add(decl.name)
                is UseDecl -> {} // imports don't add names
                is PubDecl -> {
                    val inner = decl.inner
                    when (inner) {
                        is FnDecl -> names.add(inner.name)
                        is StructDecl -> names.add(inner.name)
                        is EnumDecl -> names.add(inner.name)
                        is TraitDecl -> names.add(inner.name)
                        is TypeAliasDecl -> names.add(inner.name)
                        is ModDecl -> names.add(inner.name)
                        else -> {}
                    }
                }
            }
        }
        return names
    }

    private fun resolveNestedModules(decls: List<Decl>, parentName: String) {
        for (decl in decls) {
            when (decl) {
                is ModDecl -> {
                    val fullName = "$parentName::${decl.name}"
                    val imports = decl.body?.let { collectImports(it) } ?: emptyList()
                    val declarations = decl.body?.let { collectDeclarationNames(it) } ?: emptyList()

                    moduleGraph[fullName] = ModuleInfo(
                        name = fullName,
                        fileName = decl.fileName,
                        declarations = declarations,
                        imports = imports,
                        isPublic = decl.isPublic,
                    )

                    decl.body?.let { resolveNestedModules(it, fullName) }
                }
                is PubDecl -> {
                    if (decl.inner is ModDecl) {
                        val inner = decl.inner
                        val fullName = "$parentName::${inner.name}"
                        val imports = inner.body?.let { collectImports(it) } ?: emptyList()
                        val declarations = inner.body?.let { collectDeclarationNames(it) } ?: emptyList()

                        moduleGraph[fullName] = ModuleInfo(
                            name = fullName,
                            fileName = inner.fileName,
                            declarations = declarations,
                            imports = imports,
                            isPublic = true,
                        )

                        inner.body?.let { resolveNestedModules(it, fullName) }
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Get the built module graph.
     */
    public fun moduleGraph(): Map<String, ModuleInfo> = moduleGraph.toMap()
}
