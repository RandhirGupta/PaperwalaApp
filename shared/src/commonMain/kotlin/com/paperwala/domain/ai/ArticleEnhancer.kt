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

data class EnhancedArticle(
    val article: Article,
    val aiSummary: String,
    val aiRelevanceScore: Float
)

enum class AiStatus {
    LOCAL_LLM,
    CLOUD,
    RULE_BASED
}

interface ArticleEnhancer {
    val status: AiStatus
    suspend fun enhance(
        articles: List<Article>,
        userTopics: List<TopicCategory>
    ): List<EnhancedArticle>
}
