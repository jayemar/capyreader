package com.capyreader.app.ui.articles

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import com.jocmp.capy.R
import com.jocmp.capy.articles.FontOption

fun fontOptionToFontFamily(fontOption: FontOption): FontFamily? {
    return when (fontOption) {
        FontOption.SYSTEM_DEFAULT -> null
        FontOption.ATKINSON_HYPERLEGIBLE -> Font(R.font.atkinson_hyperlegible)
        FontOption.INTER -> Font(R.font.inter)
        FontOption.JOST -> Font(R.font.jost)
        FontOption.LITERATA -> Font(R.font.literata)
        FontOption.POPPINS -> Font(R.font.poppins)
        FontOption.VOLLKORN -> Font(R.font.vollkorn)
    }?.toFontFamily()
}
