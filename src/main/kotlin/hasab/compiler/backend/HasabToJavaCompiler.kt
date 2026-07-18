package hasab.compiler.backend

import hasab.compiler.frontend.lexer.Lexer
import hasab.compiler.frontend.lexer.SourceFile
import hasab.compiler.frontend.parser.Parser
import hasab.compiler.types.TypeChecker
import hasab.compiler.types.TypeCheckResult

public data class CompilationResult(
    val javaSource: String,
    val typeCheckResult: TypeCheckResult,
    val hasErrors: Boolean,
)

public object HasabToJavaCompiler {

    public fun compile(sourceCode: String, fileName: String = "Main.hasab"): CompilationResult {
        val source = SourceFile(fileName, sourceCode)
        val lexerResult = Lexer(source).tokenize()
        val parseResult = Parser(lexerResult).parse()

        val typeChecker = TypeChecker(parseResult.module)
        val typeCheckResult = typeChecker.check()

        val generator = JavaSourceGenerator(typeCheckDiagnostics = typeCheckResult.diagnostics)
        val javaSource = generator.generate(parseResult.module)

        return CompilationResult(
            javaSource = javaSource,
            typeCheckResult = typeCheckResult,
            hasErrors = typeCheckResult.hasErrors || parseResult.hasErrors,
        )
    }

    public fun generateJava(module: hasab.compiler.frontend.ast.Module): String {
        val generator = JavaSourceGenerator()
        return generator.generate(module)
    }
}
