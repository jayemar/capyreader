package com.capyreader.app.ui.articles

import com.capyreader.app.R
import com.jocmp.capy.articles.FontOption

val FontOption.translationKey: Int
    get() = when (this) {
        FontOption.SYSTEM_DEFAULT -> R.string.font_option_system_default
        FontOption.ATKINSON_HYPERLEGIBLE -> R.string.font_option_atkinson_hyperlegible
        FontOption.INTER -> R.string.font_option_inter
        FontOption.JOST -> R.string.font_option_jost
        FontOption.LITERATA -> R.string.font_option_literata
        FontOption.POPPINS -> R.string.font_option_poppins
        FontOption.VOLLKORN -> R.string.font_option_vollkorn
    }
