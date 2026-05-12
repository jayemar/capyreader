package com.capyreader.app.ui.articles

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkReadOnScrollTest {
    @Test
    fun initialScrollPositionIsDropped() = runTest {
        val results = mutableListOf<Int>()
        // Simulate snapshotFlow emitting restored position (6) then a real user scroll (7).
        // The initial emission (6) must be dropped to prevent marking articles as read
        // when the app reopens with a saved scroll position.
        flowOf(6, 7)
            .drop(1)
            .distinctUntilChanged()
            .collect { results.add(it) }
        assertEquals("Initial restored position should be dropped", listOf(7), results)
    }

    @Test
    fun subsequentScrollsAreNotDropped() = runTest {
        val results = mutableListOf<Int>()
        // Only the first emission (-1, the initial value at position 0) is dropped.
        // All subsequent scroll positions pass through normally.
        flowOf(-1, 0, 1, 2, 3)
            .drop(1)
            .distinctUntilChanged()
            .collect { results.add(it) }
        assertEquals("Only first emission should be dropped", listOf(0, 1, 2, 3), results)
    }

    @Test
    fun negativeInitialPositionIsDropped() = runTest {
        val results = mutableListOf<Int>()
        // When position is 0, firstVisibleItemIndex - 1 = -1. Still dropped.
        flowOf(-1, 1)
            .drop(1)
            .distinctUntilChanged()
            .collect { results.add(it) }
        assertEquals(listOf(1), results)
    }
}
