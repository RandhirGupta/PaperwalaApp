package com.paperwala.domain.model

import com.paperwala.domain.ai.LlmModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPreferencesTest {

    @Test
    fun defaultPreferencesHaveExpectedValues() {
        val prefs = UserPreferences()

        assertTrue(prefs.selectedTopics.isEmpty())
        assertTrue(prefs.preferredSources.isEmpty())
        assertEquals(10, prefs.readingTimeMinutes)
        assertEquals(7, prefs.deliveryTimeHour)
        assertFalse(prefs.hasCompletedOnboarding)
        assertTrue(prefs.enableNotifications)
        assertFalse(prefs.enableLocalLlm)
        assertEquals(LlmModel.PHI_3_MINI, prefs.selectedLlmModel)
    }

    @Test
    fun copyPreservesUnchangedFields() {
        val original = UserPreferences(
            selectedTopics = listOf(TopicCategory.TECHNOLOGY, TopicCategory.SPORTS),
            readingTimeMinutes = 15,
            enableLocalLlm = true
        )

        val modified = original.copy(readingTimeMinutes = 20)

        assertEquals(listOf(TopicCategory.TECHNOLOGY, TopicCategory.SPORTS), modified.selectedTopics)
        assertEquals(20, modified.readingTimeMinutes)
        assertTrue(modified.enableLocalLlm)
    }

    @Test
    fun copyChangesSelectedModel() {
        val prefs = UserPreferences()
        val updated = prefs.copy(selectedLlmModel = LlmModel.LLAMA_3_2_1B)

        assertEquals(LlmModel.LLAMA_3_2_1B, updated.selectedLlmModel)
        assertEquals(LlmModel.PHI_3_MINI, prefs.selectedLlmModel) // original unchanged
    }

    @Test
    fun preferencesWithAllFieldsSet() {
        val prefs = UserPreferences(
            selectedTopics = listOf(TopicCategory.TECHNOLOGY),
            preferredSources = listOf("The Hindu", "NDTV"),
            readingTimeMinutes = 5,
            deliveryTimeHour = 8,
            hasCompletedOnboarding = true,
            enableNotifications = false,
            enableLocalLlm = true,
            selectedLlmModel = LlmModel.GEMMA_2_2B
        )

        assertEquals(1, prefs.selectedTopics.size)
        assertEquals(2, prefs.preferredSources.size)
        assertEquals(5, prefs.readingTimeMinutes)
        assertEquals(8, prefs.deliveryTimeHour)
        assertTrue(prefs.hasCompletedOnboarding)
        assertFalse(prefs.enableNotifications)
        assertTrue(prefs.enableLocalLlm)
        assertEquals(LlmModel.GEMMA_2_2B, prefs.selectedLlmModel)
    }
}
