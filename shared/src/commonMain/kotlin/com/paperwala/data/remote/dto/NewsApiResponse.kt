package com.paperwala.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsApiResponse(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<NewsApiArticle> = emptyList()
)

@Serializable
data class NewsApiArticle(
    val source: NewsApiSource = NewsApiSource(),
    val author: String? = null,
    val title: String = "",
    val description: String? = null,
    val url: String = "",
    val urlToImage: String? = null,
    val publishedAt: String = "",
    val content: String? = null
)

@Serializable
data class NewsApiSource(
    val id: String? = null,
    val name: String = ""
)
