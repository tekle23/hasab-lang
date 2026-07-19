package hasab.compiler.frontend.lexer

public data class Keyword(
    public val latin: String,
    public val amharic: String,
    public val tokenString: String,
    public val amharicAlt: String? = null,
) {
    init {
        require(latin.isNotEmpty()) { "Latin keyword must not be empty" }
        require(amharic.isNotEmpty()) { "Amharic keyword must not be empty" }
        require(tokenString.isNotEmpty()) { "Token string must not be empty" }
    }

    public val latinChars: CharArray get() = latin.toCharArray()
    public val amharicChars: CharArray get() = amharic.toCharArray()

    public companion object {
        private val registry: Map<String, Keyword> by lazy {
            buildMap {
                for (kw in ALL) {
                    put(kw.latin, kw)
                    put(kw.amharic, kw)
                    if (kw.amharicAlt != null) put(kw.amharicAlt, kw)
                }
            }
        }

        public val ALL: List<Keyword> = listOf(
            Keyword("fn",       "ተግባር",     "fn"),
            Keyword("let",      "ይሁን",         "let"),
            Keyword("mut",      "ተለዋዋጭ",  "mut"),
            Keyword("if",       "ከሆነ",       "if"),
            Keyword("else",     "ካልሆነ",     "else"),
            Keyword("while",    "እስከሆነ",  "while"),
            Keyword("for",      "አምልኮ",     "for"),
            Keyword("return",   "ተመለስ",     "return"),
            Keyword("break",    "አቋርጥ",     "break"),
            Keyword("continue", "ቀጥል",       "continue"),
            Keyword("true",     "እውነት",     "true"),
            Keyword("false",    "ሐሰት",       "false"),
            Keyword("nil",      "ባዶ",       "nil"),
            Keyword("struct",   "መዋቅር", "struct"),
            Keyword("enum",     "ዝርዝርአይነት",     "enum"),
            Keyword("impl",     "impl",    "impl"),
            Keyword("trait",    "ባህሪ",       "trait"),
            Keyword("pub",      "ይፋ",     "pub"),
            Keyword("mod",      "mod",     "mod"),
            Keyword("use",      "ተጠቀም",     "use"),
            Keyword("as",       "እንደ",       "as"),
            Keyword("type",     "ዓይነት",     "type"),
            Keyword("class",    "ክፍል",   "class"),
            Keyword("new",      "አዲስ",       "new"),
            Keyword("this",     "ይህ",        "this"),
            Keyword("super",    "ወላጅ",        "super"),
            Keyword("static",   "static",     "static"),
            Keyword("void",     "void",     "void"),
            Keyword("int",      "int",     "int"),
            Keyword("float",    "float",    "float"),
            Keyword("string",   "ሐረግ",       "string"),
            Keyword("bool",     "bool",  "bool"),
            Keyword("char",     "ፊደል",       "char"),
            Keyword("import",   "አስገባ",     "import"),
            Keyword("package",  "ጥቅል",       "package"),
            Keyword("println",  "ጻፍ",         "println"),
            Keyword("main",     "ዋና",       "main"),
        )

        public fun lookup(text: String): Keyword? = registry[text]

        public fun isKeyword(text: String): Boolean = registry.containsKey(text)
    }
}
