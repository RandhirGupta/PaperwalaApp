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
import com.paperwala.domain.model.ReadingStreak
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class ReadingStreakRepository(
    private val database: PaperwalaDatabase
) {

    fun getReadingStreak(): ReadingStreak {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date

        // Query last 90 days of reading history
        val ninetyDaysAgo = now.minus(90.days).toEpochMilliseconds()
        val readTimestamps = database.readingHistoryQueries
            .getReadDatesInRange(ninetyDaysAgo, now.toEpochMilliseconds())
            .executeAsList()

        // Convert epoch millis to distinct LocalDates
        val readDates = readTimestamps
            .map { Instant.fromEpochMilliseconds(it).toLocalDateTime(tz).date }
            .distinct()
            .sorted()

        val totalArticlesRead = database.readingHistoryQueries
            .getTotalArticlesRead()
            .executeAsOne()
            .toInt()

        val currentStreak = computeCurrentStreak(readDates, today)
        val longestStreak = computeLongestStreak(readDates)
        val totalEditionsRead = readDates.size // Each distinct date = 1 edition

        return ReadingStreak(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalEditionsRead = totalEditionsRead,
            totalArticlesRead = totalArticlesRead,
            lastReadDate = readDates.lastOrNull(),
            streakDates = readDates
        )
    }

    fun getReadDatesForMonth(year: Int, month: Int): List<LocalDate> {
        val tz = TimeZone.currentSystemDefault()

        // Calculate month start and end as epoch millis
        val monthStart = LocalDate(year, month, 1)
        val monthEnd = if (month == 12) {
            LocalDate(year + 1, 1, 1)
        } else {
            LocalDate(year, month + 1, 1)
        }

        val startMillis = monthStart.atStartOfDayIn(tz).toEpochMilliseconds()
        val endMillis = monthEnd.atStartOfDayIn(tz).toEpochMilliseconds() - 1

        val timestamps = database.readingHistoryQueries
            .getReadDatesInRange(startMillis, endMillis)
            .executeAsList()

        return timestamps
            .map { Instant.fromEpochMilliseconds(it).toLocalDateTime(tz).date }
            .distinct()
    }

    private fun computeCurrentStreak(sortedDates: List<LocalDate>, today: LocalDate): Int {
        if (sortedDates.isEmpty()) return 0

        var streak = 0
        var checkDate = today

        // If the user hasn't read today, check if they read yesterday
        // (streak is still alive until end of day)
        if (checkDate !in sortedDates) {
            checkDate = today.minus(1, DateTimeUnit.DAY)
            if (checkDate !in sortedDates) return 0
        }

        // Walk backwards counting consecutive days
        while (checkDate in sortedDates) {
            streak++
            checkDate = checkDate.minus(1, DateTimeUnit.DAY)
        }

        return streak
    }

    private fun computeLongestStreak(sortedDates: List<LocalDate>): Int {
        if (sortedDates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until sortedDates.size) {
            val prev = sortedDates[i - 1]
            val curr = sortedDates[i]
            val daysBetween = curr.toEpochDays() - prev.toEpochDays()

            if (daysBetween == 1) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }

        return longest
    }
}
