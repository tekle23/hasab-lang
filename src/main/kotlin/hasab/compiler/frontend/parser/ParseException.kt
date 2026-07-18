package hasab.compiler.frontend.parser

import hasab.compiler.frontend.lexer.Token
import hasab.compiler.frontend.lexer.TokenType

public class ParseException(
    public val token: Token,
    message: String,
) : RuntimeException(message)
