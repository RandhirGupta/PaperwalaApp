package com.paperwala.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TopicCategory(val displayName: String, val icon: String) {
    POLITICS("Politics", "politics"),
    TECHNOLOGY("Technology", "technology"),
    SPORTS("Sports", "sports"),
    BUSINESS("Business", "business"),
    ENTERTAINMENT("Entertainment", "entertainment"),
    SCIENCE("Science", "science"),
    HEALTH("Health", "health"),
    WORLD_NEWS("World News", "world"),
    INDIA("India", "india"),
    OPINION("Opinion", "opinion"),
    ENVIRONMENT("Environment", "environment"),
    EDUCATION("Education", "education");

    companion object {
        fun fromString(value: String): TopicCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: WORLD_NEWS
        }
    }
}
