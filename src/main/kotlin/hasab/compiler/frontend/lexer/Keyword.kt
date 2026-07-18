package hasab.compiler.frontend.lexer

public data class Keyword(
    public val latin: String,
    public val amharic: String,
    public val tokenString: String,
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
                }
            }
        }

        public val ALL: List<Keyword> = listOf(
            Keyword("fn",       "ተግባር",     "fn"),
            Keyword("let",      "ለ",         "let"),
            Keyword("mut",      "ለተቀይညት",  "mut"),
            Keyword("if",       "ከመ",       "if"),
            Keyword("else",     "አይደለ",     "else"),
            Keyword("while",    "በ.repeat",  "while"),
            Keyword("for",      "አምልኮ",     "for"),
            Keyword("return",   "ተመለስ",     "return"),
            Keyword("break",    "ውቅያና",     "break"),
            Keyword("continue", "ቀጥል",       "continue"),
            Keyword("true",     "እውነት",     "true"),
            Keyword("false",    "ሐሰት",       "false"),
            Keyword("nil",      "ባዶ",       "nil"),
            Keyword("struct",   "ሰንstructor", "struct"),
            Keyword("enum",     "ማሰሪያ",     "enum"),
            Keyword("impl",     "ተግባርለ",    "impl"),
            Keyword("trait",    "ባህሪ",       "trait"),
            Keyword("pub",      "ተጨማሪ",     "pub"),
            Keyword("mod",      "ሰብሰብ",     "mod"),
            Keyword("use",      "አድርግ",     "use"),
            Keyword("as",       "እንደ",       "as"),
            Keyword("type",     "ዓይነት",     "type"),
            Keyword("class",    "ชั้นเรียน",   "class"),
            Keyword("new",      "አዲስ",       "new"),
            Keyword("this",     "ይህ",        "this"),
            Keyword("super",    "ላይ",        "super"),
            Keyword("static",   "ተቀጥታ",     "static"),
            Keyword("void",     "ባዶነት",     "void"),
            Keyword("int",      "ዋናብር",     "int"),
            Keyword("float",    "ብርስ浮动",    "float"),
            Keyword("string",   "ሐረግ",       "string"),
            Keyword("bool",     "እውነትlies", "bool"),
            Keyword("char",     "ፊደል",       "char"),
            Keyword("import",   "ማምጣት",     "import"),
            Keyword("package",  "ክፍል",       "package"),
        )

        public fun lookup(text: String): Keyword? = registry[text]

        public fun isKeyword(text: String): Boolean = registry.containsKey(text)
    }
}
