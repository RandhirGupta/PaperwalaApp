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
package com.paperwala.domain.usecase

import com.paperwala.data.repository.EditionRepository
import com.paperwala.data.repository.NewsRepository
import com.paperwala.data.repository.UserRepository
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.Edition
import com.paperwala.domain.model.Section
import com.paperwala.domain.model.TopicCategory
import com.paperwala.util.ReadTimeCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GenerateMorningEditionUseCase(
    private val newsRepository: NewsRepository,
    private val editionRepository: EditionRepository,
    private val userRepository: UserRepository
) {

    suspend fun execute(forceRefresh: Boolean = false): Edition {
        // Check if today's edition already exists
        val existing = editionRepository.getTodayEdition()
        if (existing != null && !forceRefresh) {
            return existing
        }

        val prefs = userRepository.getPreferences()

        // Fetch fresh news
        newsRepository.fetchAndStoreNews(prefs.preferredSources)

        // Get recent articles (last 24 hours)
        val oneDayAgo = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000L)
        val allArticles = newsRepository.getRecentArticles(oneDayAgo)

        // Build sections based on user preferences
        val sections = buildSections(allArticles, prefs.selectedTopics, prefs.readingTimeMinutes)

        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val allEditionArticles = sections.flatMap { it.articles }

        val edition = Edition(
            id = "edition-${today}",
            date = today,
            generatedAt = now,
            headline = generateHeadline(sections),
            sections = sections,
            totalReadTimeMinutes = ReadTimeCalculator.estimateEditionMinutes(
                allEditionArticles.map { it.readTimeMinutes }
            ),
            articleCount = allEditionArticles.size
        )

        // Save edition
        editionRepository.saveEdition(edition)

        return edition
    }

    private fun buildSections(
        articles: List<Article>,
        userTopics: List<TopicCategory>,
        targetReadTimeMinutes: Int
    ): List<Section> {
        val sections = mutableListOf<Section>()
        val usedArticleIds = mutableSetOf<String>()

        // Calculate how many articles we can fit
        val avgReadTime = 3 // avg minutes per article
        val maxArticles = (targetReadTimeMinutes / avgReadTime).coerceIn(5, 30)

        // "Above the Fold" — top 3 articles by relevance score (any category)
        val topArticles = articles
            .sortedByDescending { it.relevanceScore * 0.6f + (if (it.category in userTopics) 0.4f else 0f) }
            .take(3)
            .also { list -> usedArticleIds.addAll(list.map { it.id }) }

        if (topArticles.isNotEmpty()) {
            sections.add(
                Section(
                    category = TopicCategory.INDIA, // Default for front page
                    displayName = "Above the Fold",
                    articles = topArticles
                )
            )
        }

        // Fill remaining sections from user's preferred topics
        var remainingSlots = maxArticles - topArticles.size
        val topicsToFill = userTopics.ifEmpty { TopicCategory.entries.take(4) }

        for (topic in topicsToFill) {
            if (remainingSlots <= 0) break

            val sectionArticles = articles
                .filter { it.category == topic && it.id !in usedArticleIds }
                .sortedByDescending { it.relevanceScore }
                .take(minOf(3, remainingSlots))

            if (sectionArticles.isNotEmpty()) {
                sections.add(
                    Section(
                        category = topic,
                        displayName = topic.displayName,
                        articles = sectionArticles
                    )
                )
                usedArticleIds.addAll(sectionArticles.map { it.id })
                remainingSlots -= sectionArticles.size
            }
        }

        return sections
    }

    private fun generateHeadline(sections: List<Section>): String {
        val frontPage = sections.firstOrNull()
        val topArticle = frontPage?.articles?.firstOrNull()
        return topArticle?.title ?: "Your Morning Briefing"
    }
}
