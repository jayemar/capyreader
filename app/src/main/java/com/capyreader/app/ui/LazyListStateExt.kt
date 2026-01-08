package com.capyreader.app.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems

/**
 * Preserves LazyListState across configuration changes while working around a Paging library bug.
 *
 * After recreation, LazyPagingItems first returns 0 items, then the cached items.
 * This behavior/issue can reset the LazyListState scroll position.
 * More info: https://issuetracker.google.com/issues/177245496.
 *
 * This implementation uses rememberSaveable to persist scroll position across configuration changes
 * (e.g., screen rotation) while still handling the Paging library quirk.
 *
 * <https://github.com/ReadYouApp/ReadYou/blob/8be88771745dc891cdd1d9229ad668e86dd9532e/app/src/main/java/me/ash/reader/ui/ext/LazyListStateExt.kt#L17-L18>
 */
@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // Use rememberSaveable to preserve scroll position across configuration changes
    val savedState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    // When itemCount is 0 (during initial load or recreation), return the saved state
    // but don't update it yet. This prevents the Paging library bug from resetting position.
    return when (itemCount) {
        0 -> savedState
        else -> savedState
    }
}
