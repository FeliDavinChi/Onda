package com.lastwave.app.ui.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsFontScaleAdjustmentTest {
    @Test
    fun dragPreviewsEveryValueButCommitsOnlyTheFinalValue() {
        val previews = mutableListOf<Float>()
        val commits = mutableListOf<Float>()
        val adjustment = LyricsFontScaleAdjustment(1f, previews::add, commits::add)

        listOf(0.8f, 1.1f, 1.3f, 1.5f).forEach(adjustment::preview)

        assertEquals(listOf(0.8f, 1.1f, 1.3f, 1.5f), previews)
        assertTrue(commits.isEmpty())
        adjustment.finish()
        adjustment.finish()
        assertEquals(listOf(1.5f), commits)
    }

    @Test
    fun preferenceEmissionDuringDragDoesNotOverwriteTheDraft() {
        val previews = mutableListOf<Float>()
        val commits = mutableListOf<Float>()
        val adjustment = LyricsFontScaleAdjustment(1f, previews::add, commits::add)
        adjustment.preview(1.4f)
        adjustment.sync(1f)
        adjustment.finish()

        assertEquals(listOf(1.4f), previews)
        assertEquals(listOf(1.4f), commits)
        adjustment.sync(0.8f)
        assertEquals(listOf(1.4f, 0.8f), previews)
        assertEquals(listOf(1.4f), commits)
    }

    @Test
    fun presetClearsAnUnfinishedDragAndCommitsImmediatelyOnce() {
        val commits = mutableListOf<Float>()
        val adjustment = LyricsFontScaleAdjustment(1f, {}, commits::add)
        adjustment.preview(1.5f)
        adjustment.select(1f)
        adjustment.finish()
        assertEquals(listOf(1f), commits)
    }

    @Test
    fun finishingWithoutADragDoesNotPersistAnything() {
        val commits = mutableListOf<Float>()
        val adjustment = LyricsFontScaleAdjustment(1f, {}, commits::add)
        adjustment.finish()
        assertTrue(commits.isEmpty())
    }
}
