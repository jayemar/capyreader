package com.capyreader.app.ui.articles

enum class SummaryMaxLines(val lines: Int) {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    UNLIMITED(Int.MAX_VALUE);

    companion object {
        val default = TWO
    }
}
