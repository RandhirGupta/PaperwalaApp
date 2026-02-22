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
