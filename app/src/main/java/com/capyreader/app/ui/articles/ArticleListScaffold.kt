package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ArticleListScaffold(
    padding: PaddingValues,
    showOnboarding: Boolean,
    onboarding: @Composable () -> Unit,
    articles: @Composable (PaddingValues) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Don't clip refresh indicators that animate beyond bounds
                clip = false
            }
    ) {
        if (showOnboarding) {
            Box(Modifier.padding(padding)) {
                onboarding()
            }
        } else {
            articles(padding)
        }
    }
}
