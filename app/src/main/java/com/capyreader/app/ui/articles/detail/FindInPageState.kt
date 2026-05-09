package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.compositionLocalOf

data class FindInPageState(
    val isVisible: Boolean = false,
    val show: () -> Unit = {},
    val hide: () -> Unit = {},
)

val LocalFindInPage = compositionLocalOf { FindInPageState() }
