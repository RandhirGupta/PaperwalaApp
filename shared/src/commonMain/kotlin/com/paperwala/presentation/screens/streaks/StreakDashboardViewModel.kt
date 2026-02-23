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
package com.paperwala.presentation.screens.streaks

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.paperwala.data.repository.ReadingStreakRepository
import com.paperwala.domain.model.ReadingStreak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class StreakDashboardState(
    val streak: ReadingStreak = ReadingStreak(),
    val calendarDates: List<LocalDate> = emptyList(),
    val displayMonth: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
    val displayYear: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
    val isLoading: Boolean = true
)

class StreakDashboardViewModel(
    private val readingStreakRepository: ReadingStreakRepository
) : ScreenModel {

    private val _state = MutableStateFlow(StreakDashboardState())
    val state: StateFlow<StreakDashboardState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val streak = readingStreakRepository.getReadingStreak()
            val calendarDates = readingStreakRepository.getReadDatesForMonth(
                _state.value.displayYear,
                _state.value.displayMonth
            )
            _state.value = _state.value.copy(
                streak = streak,
                calendarDates = calendarDates,
                isLoading = false
            )
        }
    }

    fun previousMonth() {
        val current = _state.value
        val newMonth = if (current.displayMonth == 1) 12 else current.displayMonth - 1
        val newYear = if (current.displayMonth == 1) current.displayYear - 1 else current.displayYear
        _state.value = current.copy(displayMonth = newMonth, displayYear = newYear)
        loadCalendar()
    }

    fun nextMonth() {
        val current = _state.value
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        // Don't go beyond current month
        if (current.displayYear == now.year && current.displayMonth >= now.monthNumber) return

        val newMonth = if (current.displayMonth == 12) 1 else current.displayMonth + 1
        val newYear = if (current.displayMonth == 12) current.displayYear + 1 else current.displayYear
        _state.value = current.copy(displayMonth = newMonth, displayYear = newYear)
        loadCalendar()
    }

    private fun loadCalendar() {
        screenModelScope.launch {
            val calendarDates = readingStreakRepository.getReadDatesForMonth(
                _state.value.displayYear,
                _state.value.displayMonth
            )
            _state.value = _state.value.copy(calendarDates = calendarDates)
        }
    }
}
