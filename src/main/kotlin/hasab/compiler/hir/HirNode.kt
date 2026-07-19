package hasab.compiler.hir

import hasab.compiler.types.Type

/**
 * Base interface for all HIR (High-level Intermediate Representation) nodes.
 *
 * Key differences from AST nodes:
 * - Every node carries a resolved [Type].
 * - No source positions — the HIR is for codegen, not diagnostics.
 * - Flattened — PubDecl wrappers are removed, visibility is a property.
 * - No TypeNode references — all types are fully resolved [Type] instances.
 */
public sealed interface HirNode {
    public val type: Type
    public val hirChildren: List<HirNode> get() = emptyList()
}
