package com.paperwala.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val fullContent: String? = null,
    val sourceUrl: String,
    val sourceName: String,
    val sourceLogoUrl: String? = null,
    val imageUrl: String? = null,
    val author: String? = null,
    val publishedAt: Instant,
    val fetchedAt: Instant,
    val category: TopicCategory,
    val relevanceScore: Float = 0f,
    val readTimeMinutes: Int = 3,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false
)
