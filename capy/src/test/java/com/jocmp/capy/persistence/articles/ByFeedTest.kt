package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.InMemoryDatabaseProvider
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.fixtures.ArticleFixture
import com.jocmp.capy.fixtures.FeedFixture
import com.jocmp.capy.persistence.ArticleRecords
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ByFeedTest {
    private lateinit var database: Database
    private lateinit var articleRecords: ArticleRecords
    private lateinit var articleFixture: ArticleFixture
    private lateinit var feedFixture: FeedFixture

    @Before
    fun setup() {
        database = InMemoryDatabaseProvider.build("777")
        articleRecords = ArticleRecords(database)
        articleFixture = ArticleFixture(database)
        feedFixture = FeedFixture(database)
    }

    @Test
    fun all_summaryTruncation() = runTest {
        val summary = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent turpis nisi, hendrerit in lobortis ac, cursus quis odio. Etiam gravida lacinia sodales. Ut sodales orci a auctor blandit. Pellentesque ultrices faucibus magna sed rhoncus. Praesent vulputate finibus auctor. Sed a neque nec odio imperdiet finibus vitae ac ipsum. Cras mollis tincidunt suscipit. Donec quis dui eget sem ultrices faucibus eget efficitur lorem. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae;
        """.trimIndent()
        val article = articleFixture.create(summary = summary)

        val expectedSummary = summary.take(250)

        val articles = ByFeed(database)
            .all(
                feedIDs = listOf(article.feedID),
                status = ArticleStatus.ALL,
                sortOrder = SortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
                limit = 1,
                offset = 0,
                priority = FeedPriority.FEED,
            ).executeAsList()

        assertEquals(expected = expectedSummary, actual = articles[0].summary)
    }

    @Test
    fun all_readArticlesDuringSessionRemainInUnreadQuery() = runTest {
        // Given: Current session started at 12:00
        val sessionStart = OffsetDateTime.now()
        val feed = feedFixture.create()

        // Article marked read BEFORE session start
        val oldRead = articleFixture.create(
            id = "old-read",
            title = "Old Read Article",
            read = false,
            publishedAt = sessionStart.minusMinutes(10).toEpochSecond(),
            feed = feed
        )
        database.articlesQueries.markRead(
            read = true,
            lastReadAt = sessionStart.minusMinutes(5).toEpochSecond(),
            articleIDs = listOf(oldRead.id)
        )

        // Article marked read DURING session
        val newRead = articleFixture.create(
            id = "new-read",
            title = "New Read Article",
            read = false,
            publishedAt = sessionStart.minusMinutes(10).toEpochSecond(),
            feed = feed
        )
        database.articlesQueries.markRead(
            read = true,
            lastReadAt = sessionStart.plusMinutes(5).toEpochSecond(),
            articleIDs = listOf(newRead.id)
        )

        // Article still unread
        val unread = articleFixture.create(
            id = "unread",
            title = "Unread Article",
            read = false,
            publishedAt = sessionStart.minusMinutes(10).toEpochSecond(),
            feed = feed
        )

        val articles = ByFeed(database)
            .all(
                feedIDs = listOf(feed.id),
                status = ArticleStatus.UNREAD,
                query = null,
                since = sessionStart,
                limit = 100,
                sortOrder = SortOrder.NEWEST_FIRST,
                offset = 0,
                priority = FeedPriority.FEED
            ).executeAsList()

        // Should include: unread article AND recently-read article
        // Should exclude: article read before session
        assertEquals(expected = 2, actual = articles.size)
        val articleIds = articles.map { it.id }.toSet()
        assertTrue(articleIds.contains(unread.id))
        assertTrue(articleIds.contains(newRead.id))
    }
}
