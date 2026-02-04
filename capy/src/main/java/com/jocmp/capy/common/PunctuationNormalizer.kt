package com.jocmp.capy.common

/**
 * Normalizes fullwidth punctuation characters to their ASCII equivalents.
 *
 * Fullwidth characters are commonly used in CJK (Chinese, Japanese, Korean) typography
 * but can appear incorrectly in Latin text, causing awkward spacing issues.
 */
object PunctuationNormalizer {

    private val FULLWIDTH_TO_ASCII = mapOf(
        0xFF0C to 0x002C,  // ， → ,  (comma)
        0xFF0E to 0x002E,  // ． → .  (period)
        0xFF1A to 0x003A,  // ： → :  (colon)
        0xFF1B to 0x003B,  // ； → ;  (semicolon)
        0xFF01 to 0x0021,  // ！ → !  (exclamation)
        0xFF1F to 0x003F,  // ？ → ?  (question mark)
        0xFF08 to 0x0028,  // （ → (  (left paren)
        0xFF09 to 0x0029,  // ） → )  (right paren)
        0xFF3B to 0x005B,  // ［ → [  (left bracket)
        0xFF3D to 0x005D,  // ］ → ]  (right bracket)
        0xFF5B to 0x007B,  // ｛ → {  (left brace)
        0xFF5D to 0x007D,  // ｝ → }  (right brace)
        0xFF0D to 0x002D,  // － → -  (hyphen/minus)
        0xFF5E to 0x007E,  // ～ → ~  (tilde)
        0xFF1C to 0x003C,  // ＜ → <  (less than)
        0xFF1E to 0x003E,  // ＞ → >  (greater than)
        0xFF0F to 0x002F,  // ／ → /  (slash)
        0xFF3C to 0x005C,  // ＼ → \  (backslash)
        0xFF06 to 0x0026,  // ＆ → &  (ampersand)
        0xFF03 to 0x0023,  // ＃ → #  (hash)
        0xFF04 to 0x0024,  // ＄ → $  (dollar)
        0xFF05 to 0x0025,  // ％ → %  (percent)
        0xFF20 to 0x0040,  // ＠ → @  (at sign)
    )

    /**
     * Normalize fullwidth punctuation to ASCII equivalents.
     *
     * @param text The text to normalize
     * @param enabled Whether normalization is enabled
     * @return Normalized text if enabled, original text otherwise
     */
    fun normalize(text: String, enabled: Boolean): String {
        if (!enabled || text.isEmpty()) return text

        val sb = StringBuilder(text.length)

        text.codePoints().forEach { cp ->
            val replacement = FULLWIDTH_TO_ASCII[cp]
            sb.appendCodePoint(replacement ?: cp)
        }

        return sb.toString()
    }
}
