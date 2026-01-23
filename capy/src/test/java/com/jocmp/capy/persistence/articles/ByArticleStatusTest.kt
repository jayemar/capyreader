package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
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

class ByArticleStatusTest {
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

        val articles = ByArticleStatus(database)
            .all(
                status = ArticleStatus.ALL,
                sortOrder = SortOrder.NEWEST_FIRST,
                since = OffsetDateTime.now().minusDays(7),
                query = null,
                limit = 1,
                offset = 0,
            ).executeAsList()

        assertEquals(expected = expectedSummary, actual = articles[0].summary)
    }

    @Test
    fun all_unreadArticlesNotFilteredByLastReadAt() = runTest {
        val now = OffsetDateTime.now()
        val yesterday = now.minusDays(1)

        // Create an unread article
        val unreadArticle = articleFixture.create(
            id = "unread-1",
            title = "Unread Article",
            read = false,
            publishedAt = yesterday.toEpochSecond()
        )

        // Create a read article and mark it as read with lastReadAt in the past
        val readArticle = articleFixture.create(
            id = "read-1",
            title = "Read Article",
            read = false,
            publishedAt = yesterday.toEpochSecond()
        )
        database.articlesQueries.markRead(
            read = true,
            lastReadAt = yesterday.toEpochSecond(),
            articleIDs = listOf(readArticle.id)
        )

        // Query for unread articles with a `since` parameter that's in the future
        // This should return the unread article, regardless of its lastReadAt
        val articles = ByArticleStatus(database)
            .all(
                status = ArticleStatus.UNREAD,
                sortOrder = SortOrder.NEWEST_FIRST,
                since = now.plusHours(1),
                query = null,
                limit = 10,
                offset = 0,
            ).executeAsList()

        // Should return the unread article even though `since` is in the future
        assertEquals(expected = 1, actual = articles.size)
        assertEquals(expected = unreadArticle.id, actual = articles[0].id)
    }

    @Test
    fun all_readArticlesDoNotLeakIntoUnreadQuery() = runTest {
        val now = OffsetDateTime.now()
        val yesterday = now.minusDays(1)

        // Create an unread article
        val unreadArticle = articleFixture.create(
            id = "unread-1",
            title = "Unread Article",
            read = false,
            publishedAt = yesterday.toEpochSecond()
        )

        // Create a read article with lastReadAt in the past
        val readArticle = articleFixture.create(
            id = "read-1",
            title = "Read Article",
            read = false,
            publishedAt = yesterday.toEpochSecond()
        )
        database.articlesQueries.markRead(
            read = true,
            lastReadAt = yesterday.toEpochSecond(),
            articleIDs = listOf(readArticle.id)
        )

        // Query for unread articles with `since` set to before the read article's lastReadAt
        // The read article should NOT appear in the results due to correct SQL operator precedence
        val articles = ByArticleStatus(database)
            .all(
                status = ArticleStatus.UNREAD,
                sortOrder = SortOrder.NEWEST_FIRST,
                since = yesterday.minusHours(1),
                query = null,
                limit = 10,
                offset = 0,
            ).executeAsList()

        // Should only return the unread article, not the read one
        assertEquals(expected = 1, actual = articles.size)
        assertEquals(expected = unreadArticle.id, actual = articles[0].id)
    }
}
