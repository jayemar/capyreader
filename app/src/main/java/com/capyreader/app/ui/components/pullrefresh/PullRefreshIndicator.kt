package com.capyreader.app.ui.components.pullrefresh

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.components.rememberUpdatedSlingshot
import kotlin.math.absoluteValue

/**
 * A class to encapsulate details of different indicator sizes.
 *
 * @param size The overall size of the indicator.
 * @param arcRadius The radius of the arc.
 * @param strokeWidth The width of the arc stroke.
 * @param arrowWidth The width of the arrow.
 * @param arrowHeight The height of the arrow.
 */
@Immutable
private data class SwipeRefreshIndicatorSizes(
    val size: Dp,
    val arcRadius: Dp,
    val strokeWidth: Dp,
    val arrowWidth: Dp,
    val arrowHeight: Dp,
)

/**
 * The default/normal size values for [SwipeRefreshIndicator].
 */
private val DefaultSizes = SwipeRefreshIndicatorSizes(
    size = 40.dp,
    arcRadius = 7.5.dp,
    strokeWidth = 2.5.dp,
    arrowWidth = 10.dp,
    arrowHeight = 5.dp,
)

/**
 * The 'large' size values for [SwipeRefreshIndicator].
 */
private val LargeSizes = SwipeRefreshIndicatorSizes(
    size = 56.dp,
    arcRadius = 11.dp,
    strokeWidth = 3.dp,
    arrowWidth = 12.dp,
    arrowHeight = 6.dp,
)

/**
 * Indicator composable which is typically used in conjunction with [SwipeRefresh].
 *
 * @param state The [SwipeRefreshState] passed into the [SwipeRefresh] `indicator` block.
 * @param modifier The modifier to apply to this layout.
 * @param fade Whether the arrow should fade in/out as it is scrolled in. Defaults to true.
 * @param scale Whether the indicator should scale up/down as it is scrolled in. Defaults to false.
 * @param backgroundColor The color of the indicator background surface.
 * @param contentColor The color for the indicator's contents.
 * @param shape The shape of the indicator background surface. Defaults to [CircleShape].
 * @param largeIndication Whether the indicator should be 'large' or not. Defaults to false.
 * @param elevation The size of the shadow below the indicator.
 */
@Composable
fun SwipeRefreshIndicator(
    state: SwipeRefreshState,
    refreshTriggerDistance: Dp,
    modifier: Modifier = Modifier,
    clockwise: Boolean = true,
    fade: Boolean = true,
    scale: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
    contentColor: Color = contentColorFor(backgroundColor),
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    refreshingOffset: Dp = 16.dp,
    largeIndication: Boolean = false,
    elevation: Dp = 1.dp,
) {
    val sizes = if (largeIndication) LargeSizes else DefaultSizes

    val indicatorRefreshTrigger = with(LocalDensity.current) { refreshTriggerDistance.toPx() }

    val indicatorHeight = with(LocalDensity.current) { sizes.size.roundToPx() }
    val refreshingOffsetPx = with(LocalDensity.current) { refreshingOffset.toPx() }

    val slingshot = rememberUpdatedSlingshot(
        offsetY = state.indicatorOffset,
        maxOffsetY = indicatorRefreshTrigger,
        height = indicatorHeight,
    )

    var offset by remember { mutableStateOf(0f) }

    // If the user is currently swiping, we use the 'slingshot' offset directly
    if (state.isSwipeInProgress) {
        offset = slingshot.offset.toFloat()
    }

    LaunchedEffect(state.isSwipeInProgress, state.isRefreshing) {
        // If there's no swipe currently in progress, animate to the correct resting position
        if (!state.isSwipeInProgress) {
            val targetValue = when {
                state.isRefreshing -> indicatorHeight + refreshingOffsetPx
                clockwise -> 0f  // Top indicator: hide at top (offset 0)
                else -> -indicatorHeight.toFloat()  // Bottom indicator: hide below screen (negative offset)
            }
            animate(
                initialValue = offset,
                targetValue = targetValue
            ) { value, _ ->
                offset = value
            }
        }
    }

    val alpha = when {
        // During pull: fade in based on progress
        fade && state.isSwipeInProgress ->
            (state.indicatorOffset.absoluteValue / indicatorRefreshTrigger).coerceIn(0f, 1f)
        // During/after refresh: fully visible
        state.isRefreshing -> 1f
        // Hidden state: fade out based on offset
        else -> {
            if (clockwise) {
                // Top indicator: fade out as offset approaches 0
                (offset / indicatorHeight.toFloat()).coerceIn(0f, 1f)
            } else {
                // Bottom indicator: fade out as offset approaches negative
                ((offset + indicatorHeight.toFloat()) / indicatorHeight.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    Surface(
        modifier = modifier
            .size(size = sizes.size)
            .alpha(alpha)
            .graphicsLayer {
                // Translate the indicator according to the slingshot and the Position of the Swipe
                // to refresh.
                translationY = if (clockwise) {
                    offset - indicatorHeight
                } else {
                    indicatorHeight - offset
                }

                val scaleFraction = if (scale && !state.isRefreshing) {
                    val progress = offset / indicatorRefreshTrigger.coerceAtLeast(1f)

                    // We use LinearOutSlowInEasing to speed up the scale in
                    LinearOutSlowInEasing
                        .transform(progress)
                        .coerceIn(0f, 1f)
                } else 1f

                scaleX = scaleFraction
                scaleY = scaleFraction
            },
        shape = shape,
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier.size(sizes.size),
            contentAlignment = Alignment.Center
        ) {
            val progress = (state.indicatorOffset / indicatorRefreshTrigger).coerceIn(0f, 1f)

            // Show indeterminate spinner when refreshing or when we've reached the trigger point
            if (state.isRefreshing || (!state.isSwipeInProgress && progress >= 1f)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                // Show circular arrow icon during the pull
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            // Rotate the icon as you pull (360 degrees at 100% progress)
                            rotationZ = progress * 360f
                        }
                )
            }
        }
    }
}
