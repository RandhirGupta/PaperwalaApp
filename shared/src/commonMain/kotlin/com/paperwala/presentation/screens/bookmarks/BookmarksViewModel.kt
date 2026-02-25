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
package com.paperwala.presentation.screens.bookmarks

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.paperwala.domain.model.Article
import com.paperwala.domain.usecase.GetBookmarksUseCase
import com.paperwala.domain.usecase.ToggleBookmarkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookmarksState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true
)

class BookmarksViewModel(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(BookmarksState())
    val state: StateFlow<BookmarksState> = _state.asStateFlow()

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val articles = getBookmarksUseCase.execute()
            _state.value = BookmarksState(articles = articles, isLoading = false)
        }
    }

    fun toggleBookmark(articleId: String) {
        toggleBookmarkUseCase.execute(articleId)
        loadBookmarks()
    }
}
