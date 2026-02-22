/*
 * Copyright 2026 Randhir Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paperwala.data.repository

import com.paperwala.data.local.db.PaperwalaDatabase
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.model.UserPreferences

class UserRepository(
    private val database: PaperwalaDatabase
) {

    fun getPreferences(): UserPreferences {
        val entity = database.userPreferencesQueries.getPreferences().executeAsOneOrNull()
            ?: return UserPreferences()

        return UserPreferences(
            selectedTopics = entity.selected_topics
                .split(",")
                .filter { it.isNotBlank() }
                .map { TopicCategory.fromString(it.trim()) },
            preferredSources = entity.preferred_sources
                .split(",")
                .filter { it.isNotBlank() },
            readingTimeMinutes = entity.reading_time_minutes.toInt(),
            deliveryTimeHour = entity.delivery_time_hour.toInt(),
            hasCompletedOnboarding = entity.has_completed_onboarding == 1L,
            enableNotifications = entity.enable_notifications == 1L,
            enableLocalLlm = entity.enable_local_llm == 1L
        )
    }

    fun savePreferences(prefs: UserPreferences) {
        database.userPreferencesQueries.insertOrUpdatePreferences(
            selected_topics = prefs.selectedTopics.joinToString(",") { it.name },
            preferred_sources = prefs.preferredSources.joinToString(","),
            reading_time_minutes = prefs.readingTimeMinutes.toLong(),
            delivery_time_hour = prefs.deliveryTimeHour.toLong(),
            has_completed_onboarding = if (prefs.hasCompletedOnboarding) 1L else 0L,
            enable_notifications = if (prefs.enableNotifications) 1L else 0L,
            enable_local_llm = if (prefs.enableLocalLlm) 1L else 0L
        )
    }

    fun updateTopics(topics: List<TopicCategory>) {
        database.userPreferencesQueries.updateTopics(
            topics.joinToString(",") { it.name }
        )
    }

    fun updateSources(sources: List<String>) {
        database.userPreferencesQueries.updateSources(sources.joinToString(","))
    }

    fun updateReadingTime(minutes: Int) {
        database.userPreferencesQueries.updateReadingTime(minutes.toLong())
    }

    fun updateDeliveryTime(hour: Int) {
        database.userPreferencesQueries.updateDeliveryTime(hour.toLong())
    }

    fun markOnboardingComplete() {
        database.userPreferencesQueries.markOnboardingComplete()
    }

    fun hasCompletedOnboarding(): Boolean {
        return getPreferences().hasCompletedOnboarding
    }
}
