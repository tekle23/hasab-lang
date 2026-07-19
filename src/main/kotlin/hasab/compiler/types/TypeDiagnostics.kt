package hasab.compiler.types

import hasab.compiler.frontend.lexer.DiagnosticSeverity
import hasab.compiler.frontend.lexer.SourceRange

/**
 * Diagnostic codes for type checking errors.
 * Format: HSB3xxx (type system).
 *
 * Each code has:
 * - Default English message
 * - Amharic message (pure Amharic, no English mixing)
 * - Default suggestion template (can be overridden per-report)
 */
public enum class TypeDiagnosticCode(
    public val code: String,
    public val defaultMessage: String,
    public val amharicMessage: String,
    public val defaultSuggestion: String,
) {
    TYPE_MISMATCH(
        "HSB3001",
        "Type mismatch",
        "የዓይነት ልዩነት",
        "Change the variable type or convert the value to match",
    ),
    UNDEFINED_VARIABLE(
        "HSB3002",
        "Undefined variable",
        "ያልተገለጸ ተ棄ን",
        "Declare the variable before use, or check for typos",
    ),
    UNDEFINED_TYPE(
        "HSB3003",
        "Undefined type",
        "ያልተገለጸ ዓይነት",
        "Import the type or check for typos",
    ),
    UNDEFINED_FUNCTION(
        "HSB3004",
        "Undefined function",
        "ያልተገለጸ ተግባር",
        "Define the function or import it",
    ),
    WRONG_ARGUMENT_COUNT(
        "HSB3005",
        "Wrong number of arguments",
        "የተሳሳተ ቁጥር የምክር ማቅረብያ",
        "Add or remove arguments to match the function signature",
    ),
    ARGUMENT_TYPE_MISMATCH(
        "HSB3006",
        "Argument type mismatch",
        "የምክር ማቅረብያ ዓይነት ልዩነት",
        "Convert the argument to the expected type",
    ),
    RETURN_TYPE_MISMATCH(
        "HSB3007",
        "Return type mismatch",
        "የመመለሻ ዓይነት ልዩነት",
        "Return a value of the declared type, or change the return type",
    ),
    NOT_CALLABLE(
        "HSB3008",
        "Expression is not callable",
        "ግባባሪው አይጠራም",
        "Only functions and function pointers can be called",
    ),
    NOT_INDEXABLE(
        "HSB3009",
        "Expression is not indexable",
        "ግባባሪውን መመዝገብ አይቻልም",
        "Only arrays, strings, and maps can be indexed",
    ),
    NO_SUCH_FIELD(
        "HSB3010",
        "No such field on type",
        "ከዚህ ዓይነት ላይ እንደዚህ ሜዳ የለ",
        "Check the struct definition for available fields",
    ),
    CANNOT_ASSIGN(
        "HSB3011",
        "Cannot assign to expression",
        "ለግባባሪው መመደብ አይቻልም",
        "Assign to a variable or field instead",
    ),
    CANNOT_ASSIGN_TYPE(
        "HSB3012",
        "Cannot assign value of this type",
        "በዚህ ዓይነት ዋጋ መመደብ አይቻልም",
        "Change the variable type or convert the value",
    ),
    MUTABILITY_VIOLATION(
        "HSB3013",
        "Cannot modify immutable variable",
        "ያልተቀየረ ተ弃ን መቀየር አይቻልም",
        "Declare the variable with 'mut' to allow modification",
    ),
    NULL_SAFETY(
        "HSB3014",
        "Cannot use nil for non-optional type",
        "ለአልternative ያልሆነ ዓይነት nil መጠቀም አይቻልም",
        "Make the type optional with '?' or provide a non-nil value",
    ),
    CANNOT_ITERATE(
        "HSB3015",
        "Cannot iterate over this type",
        "በዚህ ዓይነት ላይ መድግግም አይቻልም",
        "Only arrays can be iterated with 'for'",
    ),
    ARITH_TYPE_ERROR(
        "HSB3016",
        "Arithmetic operator type error",
        "የሂሳብ ስሌት ተግባር ዓይነት ስህተት",
        "Use int or float operands for arithmetic",
    ),
    COMPARE_TYPE_ERROR(
        "HSB3017",
        "Comparison operator type error",
        "የማነጻጸሪያ ተግባር ዓይነት ስህተት",
        "Both operands must be the same comparable type",
    ),
    LOGIC_TYPE_ERROR(
        "HSB3018",
        "Logic operator requires bool",
        "የሎጂክ ተግባር 'bool' ይፈልጋል",
        "Ensure the operand evaluates to true or false",
    ),
    UNARY_TYPE_ERROR(
        "HSB3019",
        "Unary operator type error",
        "የነጠላ ተግባር ዓይነት ስህተት",
        "Check the operand type for this operator",
    ),
    CANNOT_INFER(
        "HSB3020",
        "Cannot infer type",
        "ዓይነት ማወቅ አይቻልም",
        "Add an explicit type annotation",
    ),
    TRAIT_NOT_IMPLEMENTED(
        "HSB3021",
        "Trait not fully implemented",
        "ተግባሩን ሙሉ በሙሉ አልተፈጸመም",
        "Implement all required methods from the trait",
    ),
    MISSING_TRAIT_METHOD(
        "HSB3022",
        "Missing required trait method",
        "አስፈላጊ የተግባር ዘዴ የለ",
        "Add the missing method to the implementation",
    ),
    SAFE_NAV_ON_NON_NULL(
        "HSB3023",
        "Safe navigation on non-nullable type",
        "በ non-nullable ዓይነት ላይ ደህንነቱ የተጠበቀ መድረሻ",
        "Remove '?.' — use '.' for non-nullable types",
    ),
    NULL_ASSERT_UNSAFE(
        "HSB3024",
        "Null assertion may fail at runtime",
        "የውድ ማረጋጋት በስራ ላይ ሊያሳይ ይችላል",
        "Add a null check before asserting, or use safe navigation '?.'",
    ),
    LAMBDA_TYPE_MISMATCH(
        "HSB3025",
        "Lambda type mismatch",
        "የላምባ ዓይነት ልዩነት",
        "Match the lambda signature to the expected function type",
    ),
    GENERIC_CONSTRAINT(
        "HSB3026",
        "Generic constraint not satisfied",
        "ተፈጥሮ ግዴታ አልተሞላም",
        "Ensure the type satisfies all trait bounds",
    ),
    TRAIT_METHOD_SIGNATURE(
        "HSB3027",
        "Trait method signature mismatch",
        "የተግባር ዘዴ ፊርማ ልዩነት",
        "Match the method signature exactly as declared in the trait",
    ),
}

/**
 * A single type-checking diagnostic with rich bilingual context.
 *
 * Includes:
 * - Error code and severity
 * - Human-readable message (English)
 * - Amharic message for bilingual output
 * - Expected and found types for type mismatch errors
 * - Suggested fix (English + Amharic)
 * - Source location
 * - Related declaration location (for cross-referencing)
 * - "Did you mean?" for similar names
 */
public data class TypeDiagnostic(
    val code: TypeDiagnosticCode,
    val severity: DiagnosticSeverity,
    val message: String,
    val range: SourceRange,
    val fileName: String,
    val expectedType: Type? = null,
    val foundType: Type? = null,
    val suggestion: String? = null,
    val hint: String? = null,
    val didYouMean: String? = null,
    val relatedLocation: SourceRange? = null,
) {
    /**
     * Format the diagnostic in English with full context.
     *
     * Example output:
     * ```
     * test.hasab:3:5: error[HSB3012]: Cannot assign value of this type
     *   expected: int
     *   found:    string
     *   suggestion: Change the variable type or convert the value
     *   hint: Declare with 'mut' to allow reassignment
     * ```
     */
    public fun format(): String {
        val sb = StringBuilder()
        sb.append("$fileName:${range.start.line}:${range.start.column}: ")
        sb.append(if (severity == DiagnosticSeverity.ERROR) "error" else "warning")
        sb.append("[${code.code}]: $message")
        expectedType?.let { sb.append("\n  expected: ${it.displayName}") }
        foundType?.let { sb.append("\n  found:    ${it.displayName}") }
        effectiveSuggestion()?.let { sb.append("\n  suggestion: $it") }
        hint?.let { sb.append("\n  hint: $it") }
        didYouMean?.let { sb.append("\n  did you mean: '$it'?") }
        relatedLocation?.let {
            sb.append("\n  note: related declaration at ${it.start.line}:${it.start.column}")
        }
        return sb.toString()
    }

    /**
     * Format the diagnostic in Amharic with full bilingual context.
     *
     * Example output:
     * ```
     * test.hasab:3:5: ስህተት[HSB3012]: በዚህ ዓይነት ዋጋ መመደብ አይቻልም
     *   ይፈልጋል: int
     *   ተገኝቷል: string
     *   ምክር: የተ棄ን ዓይነት ቀይር ወይም ዋጋውን ቀይር
     *   ምክር: በ 'mut' አድርግ እንዲቀይር
     * ```
     */
    public fun formatAmharic(): String {
        val sb = StringBuilder()
        sb.append("$fileName:${range.start.line}:${range.start.column}: ")
        sb.append(if (severity == DiagnosticSeverity.ERROR) "ስህተት" else "ማስጠንቀቂያ")
        sb.append("[${code.code}]: ${code.amharicMessage}")
        expectedType?.let { sb.append("\n  ይፈልጋል: ${it.displayName}") }
        foundType?.let { sb.append("\n  ተገኝቷል: ${it.displayName}") }
        effectiveSuggestion()?.let { sb.append("\n  ምክር: ${translateSuggestionToAmharic(it)}") }
        hint?.let { sb.append("\n  ምክር: ${translateHintToAmharic(it)}") }
        didYouMean?.let { sb.append("\n  ምክር: '$it' ማንበብ ነበር?") }
        relatedLocation?.let {
            sb.append("\n  ማስታወሻ: ተያያዥ መግለጫ ${it.start.line}:${it.start.column} ላይ")
        }
        return sb.toString()
    }

    /**
     * Format both English and Amharic together for full bilingual output.
     *
     * Example:
     * ```
     * test.hasab:3:5: error[HSB3012]
     *   en: Cannot assign value of this type
     *   አማ: በዚህ ዓይነት ዋጋ መመደብ አይቻልም
     *   expected: int
     *   found:    string
     *   suggestion: Change the variable type or convert the value
     *   ምክር:      የተ弃ን ዓይነት ቀይር ወይም ዋጋውን ቀይር
     * ```
     */
    public fun formatBilingual(): String {
        val sb = StringBuilder()
        sb.append("$fileName:${range.start.line}:${range.start.column}: ")
        sb.append(if (severity == DiagnosticSeverity.ERROR) "error" else "warning")
        sb.append("[${code.code}]")
        sb.append("\n  en: $message")
        sb.append("\n  አማ: ${code.amharicMessage}")
        expectedType?.let { sb.append("\n  expected: ${it.displayName}") }
        foundType?.let { sb.append("\n  found:    ${it.displayName}") }
        effectiveSuggestion()?.let { sb.append("\n  suggestion: $it") }
        effectiveSuggestion()?.let { sb.append("\n  ምክር:      ${translateSuggestionToAmharic(it)}") }
        hint?.let { sb.append("\n  hint: $it") }
        hint?.let { sb.append("\n  ምክር:   ${translateHintToAmharic(it)}") }
        didYouMean?.let { sb.append("\n  did you mean: '$it'?") }
        relatedLocation?.let {
            sb.append("\n  note: related declaration at ${it.start.line}:${it.start.column}")
        }
        return sb.toString()
    }

    private fun effectiveSuggestion(): String? = suggestion ?: code.defaultSuggestion

    /**
     * Map common suggestion patterns to Amharic.
     */
    private fun translateSuggestionToAmharic(suggestion: String): String = when {
        suggestion.contains("Change the variable type") -> "የተ弃ን ዓይነት ቀይር"
        suggestion.contains("convert the value") || suggestion.contains("Convert the") ->
            "ዋጋውን ቀይር ወይም የተ弃ን ዓይነት ቀይር"
        suggestion.contains("Declare") && suggestion.contains("before use") ->
            "ተ弃ኑን ከመጠቀም በፊት ይግለጹ"
        suggestion.contains("Declare") && suggestion.contains("mut") ->
            "'mut' በማድረግ ይቀይሩ"
        suggestion.contains("Import") -> "ወይም ትዕዛዙን ያምምፁ"
        suggestion.contains("Add or remove") -> "ለተግባሩ ፊርማ ማቅረብያ ቁጥር ያስተካክሉ"
        suggestion.contains("Convert the argument") -> "ምክሩን ወደ ይፈልጋቸው ዓይነት ያመቱ"
        suggestion.contains("Return a value") -> "የተፈጠገ ዋጋ ይመልሱ ወይም የመመለሻ ዓይነት ያስተካክሉ"
        suggestion.contains("Only functions") -> "ተግባራት ብቻ መጠራት ይችላል"
        suggestion.contains("Only arrays") -> "አረ렬owa ብቻ መመዝገብ ይችላል"
        suggestion.contains("Check the struct") -> "የstructor መግለጫ ያንበብট"
        suggestion.contains("Assign to a variable") -> "ለተ弃ን ወይም ለሜዳ ይመደብ"
        suggestion.contains("Remove '?.'") -> "'?.' ያስወግዱ — '.' ይጠቀሙ"
        suggestion.contains("Add a null check") -> "ከ null ማረጋጋት በፊት ስራ ያስይዙ ወይም '?.' ይጠቀሙ"
        suggestion.contains("Match the lambda") -> "የላምባ ፊርማ ወደ ይፈልጋቸው የተግባር ዓይነት ያስተካክሉ"
        suggestion.contains("satisfies all trait") -> "ሁሉንም የተግባር ግዴታዎች ያስተካክሉ"
        suggestion.contains("Match the method") -> "የዘዴ ፊርማ በተግባሩ ውስጥ እንደተፈጠገ በትክክል ያስተካክሉ"
        suggestion.contains("Implement all") -> "ሁሉንም አስፈላጊ ዘเดዎች ያስተካክሉ"
        suggestion.contains("Add the missing") -> "የጎድለ ዘዴ ያስተካክሉ"
        suggestion.contains("Ensure the type") -> "ዓይነቱ ሁሉንም የተግባር ግዴታዎች መጣድ እንደሚችል ያረጋግጡ"
        suggestion.contains("Use int or float") -> "ለሂሳብ 'int' ወይም 'float' ይጠቀሙ"
        suggestion.contains("Both operands") -> "ሁለቱም ተግባራት ተመሳሳይ ዓይነት መሆን አለባቸው"
        suggestion.contains("Add an explicit") -> "ግልጽ ዓይነት ማብራሪያ ያክሉ"
        suggestion.contains("Match the function") -> "ለተግባሩ ፊርማ ቁጥር ያስተካክሉ"
        else -> suggestion
    }

    private fun translateHintToAmharic(hint: String): String = when {
        hint.contains("Declare with 'mut'") -> "'mut' በማድረግ ይቀይሩ"
        hint.contains("Use '") && hint.contains("' to make") ->
            "ዓይነቱን '?' በማድረግ አልternative ያድርጉ"
        hint.contains("Declare the variable") -> "ተ弃ኑን ከመጠቀም በፊት ይግለጹ"
        hint.contains("Close with") -> "በ '*/' ይዝጉ"
        hint.contains("Close the string") -> "ቃላቱን በ '\"' ይዝጉ"
        hint.contains("Add a type annotation") -> "ዓይነት ማብራሪያ ያክሉ"
        else -> hint
    }
}
