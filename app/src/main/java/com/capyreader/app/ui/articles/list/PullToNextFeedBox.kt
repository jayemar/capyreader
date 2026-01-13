package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.articles.feeds.AngleRefreshState
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh

@Composable
fun PullToNextFeedBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    refreshState: AngleRefreshState = AngleRefreshState.STOPPED,
    onRequestNext: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    SwipeRefresh(
        onRefresh = { onRequestNext() },
        swipeEnabled = enabled,
        onTriggerThreshold = { triggerThreshold() },
        indicatorAlignment = Alignment.BottomCenter,
        refreshState = refreshState,
        refreshTriggerDistance = 48.dp,
        modifier = modifier,
    ) {
        content()
    }
}
