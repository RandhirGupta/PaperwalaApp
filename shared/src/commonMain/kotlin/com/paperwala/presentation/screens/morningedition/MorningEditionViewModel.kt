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
package com.paperwala.presentation.screens.morningedition

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.paperwala.data.repository.NewsRepository
import com.paperwala.data.repository.UserRepository
import com.paperwala.domain.model.Edition
import com.paperwala.domain.usecase.GenerateMorningEditionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MorningEditionState(
    val edition: Edition? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String = ""
)

class MorningEditionViewModel(
    private val generateEditionUseCase: GenerateMorningEditionUseCase,
    private val newsRepository: NewsRepository,
    private val userRepository: UserRepository
) : ScreenModel {

    private val _state = MutableStateFlow(MorningEditionState())
    val state: StateFlow<MorningEditionState> = _state.asStateFlow()

    init {
        loadEdition()
    }

    fun loadEdition(forceRefresh: Boolean = false) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val edition = generateEditionUseCase.execute(forceRefresh)
                _state.value = _state.value.copy(
                    edition = edition,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Could not load your edition. Pull to refresh."
                )
            }
        }
    }

    fun markArticleAsRead(articleId: String) {
        newsRepository.markArticleAsRead(articleId)
    }

    fun refresh() {
        loadEdition(forceRefresh = true)
    }
}
