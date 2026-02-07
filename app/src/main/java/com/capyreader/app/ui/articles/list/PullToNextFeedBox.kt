package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Calculate trigger distance as 15% of screen height
    // This scales appropriately across different device sizes
    val screenHeightDp = with(density) {
        configuration.screenHeightDp.dp
    }
    val refreshTriggerDistance = screenHeightDp * 0.15f

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    SwipeRefresh(
        onRefresh = { onRequestNext() },
        swipeEnabled = enabled,
        onTriggerThreshold = { triggerThreshold() },
        indicatorAlignment = Alignment.BottomCenter,
        refreshState = refreshState,
        refreshTriggerDistance = refreshTriggerDistance,
        modifier = modifier,
    ) {
        content()
    }
}
