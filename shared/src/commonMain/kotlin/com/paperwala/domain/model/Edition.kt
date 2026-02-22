package com.paperwala.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Edition(
    val id: String,
    val date: LocalDate,
    val generatedAt: Instant,
    val headline: String,
    val sections: List<Section>,
    val totalReadTimeMinutes: Int,
    val articleCount: Int,
    val isRead: Boolean = false
)
