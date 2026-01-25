package com.jocmp.capy.persistence.articles

import app.cash.sqldelight.Query
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.persistence.listMapper
import com.jocmp.capy.persistence.toStatusPair
import java.time.OffsetDateTime

class ByFeed(private val database: Database) {
    fun all(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime,
        limit: Long,
        sortOrder: SortOrder,
        offset: Long,
        priority: FeedPriority,
    ): Query<Article> {
        val (read, starred) = status.toStatusPair

        return database.articlesByFeedQueries.all(
            feedIDs = feedIDs,
            query = query,
            read = read,
            starred = starred,
            limit = limit,
            offset = offset,
            lastReadAt = mapLastRead(read, since),
            publishedSince = null,
            newestFirst = isDescendingOrder(sortOrder),
            priorities = priority.inclusivePriorities,
            mapper = ::listMapper
        )
    }

    fun unreadArticleIDs(
        status: ArticleStatus,
        feedIDs: List<String>,
        range: MarkRead,
        sortOrder: SortOrder,
        priority: FeedPriority,
        query: String?,
    ): Query<String> {
        val (_, starred) = status.toStatusPair
        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesByFeedQueries.findArticleIDs(
            feedIDs = feedIDs,
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID,
            publishedSince = null,
            newestFirst = isNewestFirst(sortOrder),
            query = query,
            priorities = priority.inclusivePriorities,
        )
    }

    fun count(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String?,
        since: OffsetDateTime?,
        priority: FeedPriority,
    ): Query<Long> {
        val (read, starred) = status.toStatusPair

        return database.articlesByFeedQueries.countAll(
            feedIDs = feedIDs,
            query = query,
            read = read,
            starred = starred,
            lastReadAt = mapLastRead(read, since),
            priorities = priority.inclusivePriorities,
            publishedSince = null
        )
    }

    suspend fun findArticlePosition(
        feedIDs: List<String>,
        status: ArticleStatus,
        targetArticleID: String,
        query: String? = null,
        sortOrder: SortOrder,
        since: OffsetDateTime,
        priority: FeedPriority,
    ): Long? = withIOContext {
        // Check if article exists first
        val articleExists = database.articlesQueries.findBy(
            articleID = targetArticleID,
            mapper = { id, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> id }
        ).executeAsOneOrNull() != null

        if (!articleExists) {
            return@withIOContext null
        }

        val (read, starred) = status.toStatusPair

        database.articlesByFeedQueries.findArticlePosition(
            feedIDs = feedIDs,
            read = read,
            starred = starred,
            targetArticleID = targetArticleID,
            lastReadAt = mapLastRead(read, since),
            publishedSince = null,
            query = query,
            newestFirst = isDescendingOrder(sortOrder),
            priorities = priority.inclusivePriorities,
        ).executeAsOneOrNull()
    }
}
