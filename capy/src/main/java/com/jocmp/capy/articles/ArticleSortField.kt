package com.jocmp.capy.articles

enum class ArticleSortField {
    PUBLISHED_AT,
    RETRIEVED_AT;

    companion object {
        val default = RETRIEVED_AT
    }
}
