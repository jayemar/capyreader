package com.capyreader.app.ui.articles

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
class MarkReadOnScrollIntegrationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun restoredScrollPositionDoesNotTriggerMarkRead() {
        val markedArticleIDs = mutableListOf<String>()
        val listState = LazyListState(firstVisibleItemIndex = 5)

        composeTestRule.setContent {
            val articles = flowOf(PagingData.from(fakeArticles(10)))
                .collectAsLazyPagingItems()
            MarkReadOnScroll(
                listState = listState,
                articles = articles,
                scrollHighWaterMark = -1,
                enabled = true,
                updateScrollHighWaterMark = {},
                markReadOnScroll = { markedArticleIDs.add(it) },
                resetScrollBehaviorOffset = {},
                resetScrollHighWaterMark = {},
            )
        }

        composeTestRule.mainClock.advanceTimeBy(600)

        assertEquals(
            "No mark-read should fire on restored scroll position",
            0,
            markedArticleIDs.size
        )
    }

    private fun fakeArticles(count: Int): List<Article> {
        val now = ZonedDateTime.now()
        return (1..count).map { index ->
            Article(
                id = "article-$index",
                feedID = "feed-1",
                title = "Article $index",
                author = null,
                contentHTML = "<p>Content $index</p>",
                url = null,
                summary = "Summary $index",
                imageURL = null,
                updatedAt = now,
                publishedAt = now,
                read = false,
                starred = false,
            )
        }
    }
}
