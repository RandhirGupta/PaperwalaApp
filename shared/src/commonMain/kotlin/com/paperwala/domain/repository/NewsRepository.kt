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
package com.paperwala.domain.repository

import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory

interface NewsRepository {
    suspend fun fetchAndStoreNews(preferredSources: List<String> = emptyList()): List<Article>
    fun getRecentArticles(sinceMillis: Long): List<Article>
    fun getTopArticles(sinceMillis: Long, limit: Int): List<Article>
    fun getArticlesByCategory(category: TopicCategory, limit: Int): List<Article>
    fun markArticleAsRead(articleId: String)
    fun toggleBookmark(articleId: String)
    fun getBookmarkedArticles(): List<Article>
    fun updateArticleSummaryAndScore(articleId: String, summary: String, relevanceScore: Float)
}
