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

import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import kotlinx.datetime.Clock

class RuleBasedEnhancer : ArticleEnhancer {

    override val status: AiStatus = AiStatus.RULE_BASED

    override suspend fun enhance(
        articles: List<Article>,
        userTopics: List<TopicCategory>
    ): List<EnhancedArticle> {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        return articles.map { article ->
            EnhancedArticle(
                article = article,
                aiSummary = extractiveSummarize(article),
                aiRelevanceScore = computeRelevanceScore(article, userTopics, nowMillis)
            )
        }
    }

    /**
     * Extractive summarization: picks the 2 most information-dense sentences
     * from the article text using a simple word-frequency scoring approach.
     */
    private fun extractiveSummarize(article: Article): String {
        val text = article.fullContent ?: article.summary
        if (text.isBlank()) return article.summary

        val sentences = splitSentences(text)
        if (sentences.size <= 2) return text.trim()

        // Build word frequency map (skip stop words)
        val wordFreqs = mutableMapOf<String, Int>()
        sentences.forEach { sentence ->
            tokenize(sentence).forEach { word ->
                if (word !in STOP_WORDS) {
                    wordFreqs[word] = (wordFreqs[word] ?: 0) + 1
                }
            }
        }

        // Score each sentence by sum of its word frequencies
        val scored = sentences.mapIndexed { index, sentence ->
            val tokens = tokenize(sentence)
            val score = if (tokens.isEmpty()) 0.0 else {
                tokens.sumOf { word ->
                    if (word in STOP_WORDS) 0 else (wordFreqs[word] ?: 0)
                }.toDouble() / tokens.size
            }
            // Boost first sentence (lead bias in news articles)
            val positionBoost = if (index == 0) 1.5 else 1.0
            Triple(index, sentence, score * positionBoost)
        }

        // Pick top 2 sentences, preserve original order
        val topTwo = scored
            .sortedByDescending { it.third }
            .take(2)
            .sortedBy { it.first }
            .map { it.second.trim() }

        return topTwo.joinToString(" ")
    }

    /**
     * Rule-based relevance scoring combining multiple signals.
     * Returns a score from 0.0 to 1.0.
     */
    private fun computeRelevanceScore(
        article: Article,
        userTopics: List<TopicCategory>,
        nowMillis: Long
    ): Float {
        var score = 0f

        // Topic match (0.4 weight)
        if (article.category in userTopics) {
            score += 0.4f
        }

        // Recency: articles within 6 hours get up to 0.3 boost, decaying to 0 at 24h
        val ageHours = (nowMillis - article.publishedAt.toEpochMilliseconds()) / 3_600_000.0f
        score += when {
            ageHours <= 6f -> 0.3f
            ageHours <= 24f -> 0.3f * (1f - (ageHours - 6f) / 18f)
            else -> 0f
        }

        // Content richness: articles with images and longer content score higher
        if (article.imageUrl != null) score += 0.1f
        val contentLength = (article.fullContent ?: article.summary).length
        score += when {
            contentLength > 1000 -> 0.15f
            contentLength > 500 -> 0.1f
            contentLength > 200 -> 0.05f
            else -> 0f
        }

        // Named entity density: articles mentioning more recognizable entities
        val entityBoost = countNamedEntities(article.title + " " + article.summary)
        score += (entityBoost * 0.01f).coerceAtMost(0.05f)

        return score.coerceIn(0f, 1f)
    }

    private fun splitSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 20 }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
    }

    private fun countNamedEntities(text: String): Int {
        // Simple heuristic: count capitalized words that aren't sentence starters
        return text.split(Regex("\\s+"))
            .drop(1) // skip first word (sentence starter)
            .count { it.firstOrNull()?.isUpperCase() == true && it.length > 2 }
    }

    companion object {
        private val STOP_WORDS = setOf(
            "the", "is", "at", "which", "on", "a", "an", "and", "or", "but",
            "in", "with", "to", "for", "of", "not", "no", "can", "had", "has",
            "have", "was", "were", "been", "being", "are", "this", "that", "its",
            "from", "by", "as", "into", "through", "during", "before", "after",
            "above", "below", "between", "out", "off", "over", "under", "again",
            "further", "then", "once", "here", "there", "when", "where", "why",
            "how", "all", "each", "every", "both", "few", "more", "most", "other",
            "some", "such", "only", "own", "same", "than", "too", "very", "just",
            "because", "about", "would", "could", "should", "will", "may", "also",
            "said", "says", "new", "one", "two", "also", "like", "even", "back",
            "year", "years", "time", "first", "last", "long", "great", "little",
            "own", "old", "right", "big", "high", "different", "small", "large",
            "next", "early", "young", "important", "public", "bad", "good"
        )
    }
}
