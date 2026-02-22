package com.paperwala.domain.model

data class Section(
    val category: TopicCategory,
    val displayName: String,
    val articles: List<Article>
)
