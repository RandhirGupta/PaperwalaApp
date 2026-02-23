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

class LocalArticleEnhancer(
    private val llmEngine: LocalLlmEngine,
    private val modelManager: ModelManager
) : ArticleEnhancer {

    override val status: AiStatus = AiStatus.LOCAL_LLM

    override suspend fun enhance(
        articles: List<Article>,
        userTopics: List<TopicCategory>
    ): List<EnhancedArticle> {
        // Ensure model is loaded
        if (!llmEngine.isModelLoaded()) {
            val modelPath = modelManager.getModelPath()
                ?: throw IllegalStateException("Model not downloaded")
            llmEngine.loadModel(modelPath)
        }

        val topicsStr = userTopics.joinToString(", ") { it.displayName }

        // Process articles sequentially to manage memory
        return articles.map { article ->
            try {
                val summary = summarizeArticle(article)
                val score = scoreRelevance(article, topicsStr)
                EnhancedArticle(
                    article = article,
                    aiSummary = summary,
                    aiRelevanceScore = score
                )
            } catch (e: Exception) {
                EnhancedArticle(
                    article = article,
                    aiSummary = article.summary,
                    aiRelevanceScore = article.relevanceScore
                )
            }
        }
    }

    private suspend fun summarizeArticle(article: Article): String {
        val content = (article.fullContent ?: article.summary).take(1000)
        val prompt = """<|user|>
Summarize this news article in 2-3 concise sentences:

Title: ${article.title}
Content: $content
<|end|>
<|assistant|>"""

        val response = llmEngine.generate(prompt, maxTokens = 150)
        return response.trim().ifBlank { article.summary }
    }

    private suspend fun scoreRelevance(article: Article, topicsStr: String): Float {
        val prompt = """<|user|>
Rate the relevance of this article for a reader interested in: $topicsStr
Respond with ONLY a number from 0 to 100.

Title: ${article.title}
Category: ${article.category.displayName}
Summary: ${article.summary.take(200)}
<|end|>
<|assistant|>"""

        val response = llmEngine.generate(prompt, maxTokens = 10)
        return try {
            response.trim().filter { it.isDigit() }.take(3).toFloat().coerceIn(0f, 100f) / 100f
        } catch (e: Exception) {
            0.5f
        }
    }
}
