package com.paperwala.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GNewsResponse(
    val totalArticles: Int = 0,
    val articles: List<GNewsArticle> = emptyList()
)

@Serializable
data class GNewsArticle(
    val title: String = "",
    val description: String? = null,
    val content: String? = null,
    val url: String = "",
    val image: String? = null,
    val publishedAt: String = "",
    val source: GNewsSource = GNewsSource()
)

@Serializable
data class GNewsSource(
    val name: String = "",
    val url: String = ""
)
