package hasab.compiler.frontend.lexer

public sealed class TokenType {

    // ── Literals ──────────────────────────────────────────────
    public data object IntegerLiteral : TokenType()
    public data object FloatLiteral : TokenType()
    public data object StringLiteral : TokenType()
    public data object CharacterLiteral : TokenType()

    // ── Identifiers & Keywords ────────────────────────────────
    public data object Identifier : TokenType()
    public data class Keyword(val keyword: hasab.compiler.frontend.lexer.Keyword) : TokenType()

    // ── Operators ─────────────────────────────────────────────
    public data object Plus : TokenType()             // +
    public data object Minus : TokenType()            // -
    public data object Star : TokenType()             // *
    public data object Slash : TokenType()            // /
    public data object Percent : TokenType()          // %
    public data object Assign : TokenType()           // =
    public data object Equal : TokenType()            // ==
    public data object NotEqual : TokenType()         // !=
    public data object Less : TokenType()             // <
    public data object Greater : TokenType()          // >
    public data object LessEqual : TokenType()        // <=
    public data object GreaterEqual : TokenType()     // >=
    public data object And : TokenType()              // &&
    public data object Or : TokenType()               // ||
    public data object Not : TokenType()              // !
    public data object BitwiseAnd : TokenType()       // &
    public data object BitwiseOr : TokenType()        // |
    public data object BitwiseXor : TokenType()       // ^
    public data object BitwiseNot : TokenType()       // ~
    public data object ShiftLeft : TokenType()        // <<
    public data object ShiftRight : TokenType()       // >>
    public data object PlusAssign : TokenType()       // +=
    public data object MinusAssign : TokenType()      // -=
    public data object StarAssign : TokenType()       // *=
    public data object SlashAssign : TokenType()      // /=
    public data object PercentAssign : TokenType()    // %=
    public data object Arrow : TokenType()            // ->
    public data object FatArrow : TokenType()         // =>
    public data object DoubleColon : TokenType()      // ::
    public data object Dot : TokenType()              // .
    public data object RangeInclusive : TokenType()   // ..=
    public data object RangeExclusive : TokenType()   // ..

    // ── Delimiters ────────────────────────────────────────────
    public data object LeftParen : TokenType()        // (
    public data object RightParen : TokenType()       // )
    public data object LeftBrace : TokenType()        // {
    public data object RightBrace : TokenType()       // }
    public data object LeftBracket : TokenType()      // [
    public data object RightBracket : TokenType()     // ]
    public data object Semicolon : TokenType()        // ;
    public data object Comma : TokenType()            // ,
    public data object Colon : TokenType()            // :
    public data object Underscore : TokenType()       // _

    // ── Special ───────────────────────────────────────────────
    public data object Eof : TokenType()

    override fun toString(): String = this::class.simpleName ?: "TokenType"
}
