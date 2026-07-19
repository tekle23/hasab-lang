package hasab.compiler.backend

import hasab.compiler.frontend.ast.Module

public class JavaSourceBackend(
    private val generator: JavaSourceGenerator,
) : CompilerBackend {

    override val backendType: BackendType = BackendType.JAVA_SOURCE

    override fun generate(context: BackendContext): BackendOutput {
        val generatedSources = mutableMapOf<String, String>()
        var mainClassName = ""

        for (module in context.modules) {
            val rawClassName = module.fileName
                .removeSuffix(".has")
                .removeSuffix(".hasab")
                .substringAfterLast("/")
                .substringAfterLast("\\")
                .replace(Regex("[^A-Za-z0-9_]"), "_")
                .let { if (it.isEmpty() || it[0].isDigit()) "_$it" else it }
            val javaFileName = "$rawClassName.java"
            val className = rawClassName
            val javaSource = generator.generate(module.ast, module.typeCheckResult, context.sourceMap, module.fileName, javaFileName)

            generatedSources[javaFileName] = javaSource
            context.sourceMap.recordClassMapping(className, module.fileName)

            if (isEntryPoint(module)) {
                mainClassName = className
            }
        }

        if (mainClassName.isEmpty() && generatedSources.isNotEmpty()) {
            mainClassName = generatedSources.keys.first()
                .removeSuffix(".java")
        }

        return BackendOutput(generatedSources = generatedSources, mainClassName = mainClassName)
    }

    private fun isEntryPoint(module: CompiledModule): Boolean {
        for (decl in module.ast.declarations) {
            if (decl is hasab.compiler.frontend.ast.FnDecl && decl.name == "main") {
                return true
            }
        }
        return false
    }
}
