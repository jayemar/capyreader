package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

@OptIn(FlowPreview::class)
@Composable
internal fun MarkReadOnScroll(
    listState: LazyListState,
    articles: LazyPagingItems<Article>,
    scrollHighWaterMark: Int,
    enabled: Boolean,
    updateScrollHighWaterMark: (Int) -> Unit,
    markReadOnScroll: (String) -> Unit,
    resetScrollBehaviorOffset: () -> Unit,
    resetScrollHighWaterMark: () -> Unit,
) {
    if (enabled) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    resetScrollHighWaterMark()
                    resetScrollBehaviorOffset()
                }
        }

        LaunchedEffect(listState) {
            snapshotFlow {
                listState.firstVisibleItemIndex - 1
            }
                .drop(1)
                .distinctUntilChanged()
                .debounce(500)
                .collect { scrolledPastIndex ->
                    CapyLog.debug(
                        "mark_read_on_scroll:collect", mapOf(
                            "scrolledPastIndex" to scrolledPastIndex,
                            "highWaterMark" to scrollHighWaterMark,
                            "itemCount" to articles.itemCount,
                        )
                    )
                    if (scrolledPastIndex > scrollHighWaterMark && scrolledPastIndex < articles.itemCount) {
                        updateScrollHighWaterMark(scrolledPastIndex)
                        val boundaryArticle = articles[scrolledPastIndex]
                        if (boundaryArticle != null) {
                            CapyLog.debug(
                                "mark_read_on_scroll:boundary", mapOf(
                                    "articleID" to boundaryArticle.id,
                                    "index" to scrolledPastIndex,
                                )
                            )
                            markReadOnScroll(boundaryArticle.id)
                        }
                    }
                }
        }
    }
}
