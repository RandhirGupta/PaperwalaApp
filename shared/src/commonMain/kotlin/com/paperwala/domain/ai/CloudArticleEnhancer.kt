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
package com.paperwala.domain.ai

import com.paperwala.data.remote.api.GeminiApiService
import com.paperwala.data.remote.dto.ArticleSummaryResult
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import kotlinx.serialization.json.Json

class CloudArticleEnhancer(
    private val geminiApiService: GeminiApiService,
    private val apiKey: String
) : ArticleEnhancer {

    override val status: AiStatus = AiStatus.CLOUD

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun enhance(
        articles: List<Article>,
        userTopics: List<TopicCategory>
    ): List<EnhancedArticle> {
        if (apiKey.isBlank() || articles.isEmpty()) {
            return articles.map { EnhancedArticle(it, it.summary, it.relevanceScore) }
        }

        val results = mutableListOf<EnhancedArticle>()

        // Process in batches of 5 to stay within token limits
        articles.chunked(BATCH_SIZE).forEach { batch ->
            try {
                val batchResults = enhanceBatch(batch, userTopics)
                results.addAll(batchResults)
            } catch (e: Exception) {
                // On failure, return articles with original data
                results.addAll(batch.map {
                    EnhancedArticle(it, it.summary, it.relevanceScore)
                })
            }
        }

        return results
    }

    private suspend fun enhanceBatch(
        articles: List<Article>,
        userTopics: List<TopicCategory>
    ): List<EnhancedArticle> {
        val topicsStr = userTopics.joinToString(", ") { it.displayName }
        val prompt = buildPrompt(articles, topicsStr)

        val response = geminiApiService.generateContent(
            apiKey = apiKey,
            prompt = prompt,
            temperature = 0.3f,
            maxOutputTokens = 2048
        )

        val responseText = response.candidates
            .firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

        return parseResponse(responseText, articles)
    }

    private fun buildPrompt(articles: List<Article>, topicsStr: String): String {
        val articlesBlock = articles.mapIndexed { index, article ->
            val content = (article.fullContent ?: article.summary).take(500)
            """
            Article ${index}:
            Title: ${article.title}
            Source: ${article.sourceName}
            Category: ${article.category.displayName}
            Content: $content
            """.trimIndent()
        }.joinToString("\n\n")

        return """
You are a news editor for a morning briefing app. For each article below:
1. Write a concise 2-3 sentence summary capturing the key facts
2. Rate relevance (0-100) for a reader interested in: $topicsStr

Respond as a JSON array with objects containing: "index" (int), "summary" (string), "relevanceScore" (int 0-100).

$articlesBlock
        """.trimIndent()
    }

    private fun parseResponse(
        responseText: String,
        articles: List<Article>
    ): List<EnhancedArticle> {
        return try {
            val parsed = json.decodeFromString<List<ArticleSummaryResult>>(responseText)
            articles.mapIndexed { index, article ->
                val result = parsed.firstOrNull { it.index == index }
                EnhancedArticle(
                    article = article,
                    aiSummary = result?.summary ?: article.summary,
                    aiRelevanceScore = (result?.relevanceScore ?: 50).toFloat() / 100f
                )
            }
        } catch (e: Exception) {
            // If JSON parsing fails, return articles with original data
            articles.map { EnhancedArticle(it, it.summary, it.relevanceScore) }
        }
    }

    companion object {
        private const val BATCH_SIZE = 5
    }
}
