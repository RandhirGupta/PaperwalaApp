package com.paperwala.domain.model

import kotlinx.datetime.LocalDate

data class ReadingStreak(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalEditionsRead: Int = 0,
    val totalArticlesRead: Int = 0,
    val lastReadDate: LocalDate? = null,
    val streakDates: List<LocalDate> = emptyList()
)
