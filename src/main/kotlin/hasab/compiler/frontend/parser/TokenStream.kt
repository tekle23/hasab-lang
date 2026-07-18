package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType

public class TokenStream(private val tokens: List<Token>) {
    private var position: Int = 0

    public val size: Int get() = tokens.size

    public fun peek(): Token = tokens[position.coerceAtMost(tokens.lastIndex)]

    public fun peekAt(offset: Int): Token {
        val idx = (position + offset).coerceIn(0, tokens.lastIndex)
        return tokens[idx]
    }

    public fun advance(): Token {
        val token = peek()
        if (position < tokens.lastIndex) {
            position++
        }
        return token
    }

    public fun expect(type: TokenType): Token {
        val token = peek()
        if (token.type != type) {
            throw ParseException(token, "Expected ${type}, got ${token.type}")
        }
        return advance()
    }

    public fun match(type: TokenType): Token? {
        return if (peek().type == type) advance() else null
    }

    public fun matchAny(vararg types: TokenType): Token? {
        val token = peek()
        return if (types.any { it == token.type }) advance() else null
    }

    public fun isAt(type: TokenType): Boolean = peek().type == type

    public fun isAtAny(vararg types: TokenType): Boolean = types.any { it == peek().type }

    public fun isAtEnd(): Boolean = peek().type == TokenType.Eof

    public fun currentPosition(): Int = position

    public fun saveState(): Int = position

    public fun restoreState(state: Int) {
        position = state
    }

    public fun currentToken(): Token = peek()

    public fun previousToken(): Token {
        return if (position > 0) tokens[position - 1] else tokens[0]
    }

    public fun remaining(): List<Token> = tokens.subList(position, tokens.size)

    public fun skipTo(type: TokenType): Token {
        while (!isAt(type) && !isAtEnd()) {
            advance()
        }
        return peek()
    }

    public fun tokensBetween(start: Int, end: Int): List<Token> =
        tokens.subList(start.coerceIn(0, tokens.lastIndex), end.coerceIn(0, tokens.size))

    public fun isAtKeyword(keywordString: String): Boolean {
        val token = peek()
        val type = token.type
        return type is TokenType.Keyword && type.keyword.tokenString == keywordString
    }

    public fun matchKeyword(keywordString: String): Token? {
        return if (isAtKeyword(keywordString)) advance() else null
    }

    public fun expectKeyword(keywordString: String): Token {
        val token = peek()
        if (token.type !is TokenType.Keyword ||
            (token.type as TokenType.Keyword).keyword.tokenString != keywordString
        ) {
            throw ParseException(token, "Expected keyword '$keywordString', got ${token.type}")
        }
        return advance()
    }
}
