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
import com.paperwala.domain.ai.ArticleEnhancerFactory
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.Edition
import com.paperwala.domain.model.Section
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.model.UserPreferences
import com.paperwala.util.ReadTimeCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GenerateMorningEditionUseCase(
    private val newsRepository: NewsRepository,
    private val editionRepository: EditionRepository,
    private val userRepository: UserRepository,
    private val articleEnhancerFactory: ArticleEnhancerFactory? = null
) {

    suspend fun execute(forceRefresh: Boolean = false): Edition {
        // Check if today's edition already exists (skip cache if it was empty)
        val existing = editionRepository.getTodayEdition()
        if (existing != null && !forceRefresh && existing.articleCount > 0) {
            return existing
        }

        val prefs = userRepository.getPreferences()

        // Fetch fresh news
        newsRepository.fetchAndStoreNews(prefs.preferredSources)

        // Get recent articles (last 24 hours)
        val oneDayAgo = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000L)
        val allArticles = newsRepository.getRecentArticles(oneDayAgo)

        // Enhance articles with AI (summarization + relevance scoring)
        val enhancedArticles = enhanceArticles(allArticles, prefs)

        // Build sections based on user preferences
        val sections = buildSections(enhancedArticles, prefs.selectedTopics, prefs.readingTimeMinutes)

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

    private suspend fun enhanceArticles(
        articles: List<Article>,
        prefs: UserPreferences
    ): List<Article> {
        if (articleEnhancerFactory == null) return articles

        val enhancer = try {
            articleEnhancerFactory.create(prefs)
        } catch (e: Exception) {
            articleEnhancerFactory.fallback()
        }

        return try {
            val enhanced = enhancer.enhance(articles, prefs.selectedTopics)
            enhanced.map { result ->
                // Persist AI summary and score to DB
                newsRepository.updateArticleSummaryAndScore(
                    articleId = result.article.id,
                    summary = result.aiSummary,
                    relevanceScore = result.aiRelevanceScore
                )
                result.article.copy(
                    summary = result.aiSummary,
                    relevanceScore = result.aiRelevanceScore
                )
            }
        } catch (e: Exception) {
            // Fallback to rule-based if primary enhancer fails
            try {
                val fallbackResults = articleEnhancerFactory.fallback()
                    .enhance(articles, prefs.selectedTopics)
                fallbackResults.map { result ->
                    result.article.copy(
                        relevanceScore = result.aiRelevanceScore
                    )
                }
            } catch (e2: Exception) {
                articles
            }
        }
    }

    private fun buildSections(
        articles: List<Article>,
        userTopics: List<TopicCategory>,
        targetReadTimeMinutes: Int
    ): List<Section> {
        val sections = mutableListOf<Section>()
        val usedArticleIds = mutableSetOf<String>()
        val nowMillis = Clock.System.now().toEpochMilliseconds()

        // Calculate how many articles we can fit
        val avgReadTime = 3 // avg minutes per article
        val maxArticles = (targetReadTimeMinutes / avgReadTime).coerceIn(5, 30)

        // "Above the Fold" — top 3 by composite score, with source diversity (max 1 per source)
        val topArticles = articles
            .sortedByDescending { compositeScore(it, userTopics, nowMillis) }
            .enforceDiversity(maxPerSource = 1)
            .take(3)
            .also { list -> usedArticleIds.addAll(list.map { it.id }) }

        if (topArticles.isNotEmpty()) {
            sections.add(
                Section(
                    category = TopicCategory.INDIA,
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
                .sortedByDescending { compositeScore(it, userTopics, nowMillis) }
                .enforceDiversity(maxPerSource = 2)
                .take(minOf(5, remainingSlots))

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

    /** Composite score: relevance (50%) + recency boost (20%) + topic match (30%) */
    private fun compositeScore(
        article: Article,
        userTopics: List<TopicCategory>,
        nowMillis: Long
    ): Float {
        val relevance = article.relevanceScore * 0.5f

        // Recency: articles within 6 hours get up to 0.2 boost, linearly decaying to 0 at 24h
        val ageHours = (nowMillis - article.publishedAt.toEpochMilliseconds()) / 3_600_000.0f
        val recencyBoost = when {
            ageHours <= 6f -> 0.2f
            ageHours <= 24f -> 0.2f * (1f - (ageHours - 6f) / 18f)
            else -> 0f
        }

        // Topic match: preferred topics get a 0.3 boost
        val topicMatch = if (article.category in userTopics) 0.3f else 0f

        return relevance + recencyBoost + topicMatch
    }

    /** Enforce source diversity: keep at most [maxPerSource] articles from the same source. */
    private fun List<Article>.enforceDiversity(maxPerSource: Int): List<Article> {
        val sourceCounts = mutableMapOf<String, Int>()
        return filter { article ->
            val count = sourceCounts.getOrPut(article.sourceName) { 0 }
            if (count < maxPerSource) {
                sourceCounts[article.sourceName] = count + 1
                true
            } else {
                false
            }
        }
    }

    private fun generateHeadline(sections: List<Section>): String {
        val frontPage = sections.firstOrNull()
        val topArticle = frontPage?.articles?.firstOrNull()
        return topArticle?.title ?: "Your Morning Briefing"
    }
}
