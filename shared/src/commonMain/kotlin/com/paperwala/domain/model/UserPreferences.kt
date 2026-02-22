package com.paperwala.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val selectedTopics: List<TopicCategory> = emptyList(),
    val preferredSources: List<String> = emptyList(),
    val readingTimeMinutes: Int = 10,
    val deliveryTimeHour: Int = 7,
    val hasCompletedOnboarding: Boolean = false,
    val enableNotifications: Boolean = true,
    val enableLocalLlm: Boolean = false
)
