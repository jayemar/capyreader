package com.jocmp.capy.persistence.articles

import com.jocmp.capy.articles.SortOrder
import java.time.OffsetDateTime

internal fun isDescendingOrder(sortOrder: SortOrder) =
    sortOrder == SortOrder.NEWEST_FIRST

internal fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    // For unread articles (read = false), use the session start time to filter out
    // articles that were already read before the session, while keeping articles
    // marked read during the session visible
    // For read articles (read = true), use the session start time normally
    // For all articles (read = null), return null to show everything
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}

