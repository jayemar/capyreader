package com.jocmp.capy.persistence.articles

import com.jocmp.capy.articles.SortOrder
import java.time.OffsetDateTime

internal fun isDescendingOrder(sortOrder: SortOrder) =
    sortOrder == SortOrder.NEWEST_FIRST

internal fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    // Only filter by lastReadAt for read articles (read = true)
    // For unread articles (read = false), we want to show all articles regardless of when they were read
    if (read == true) {
        return value?.toEpochSecond()
    }

    return null
}

