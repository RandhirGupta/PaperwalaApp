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
package com.paperwala.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TopicCategory(val displayName: String, val icon: String) {
    POLITICS("Politics", "politics"),
    TECHNOLOGY("Technology", "technology"),
    SPORTS("Sports", "sports"),
    BUSINESS("Business", "business"),
    ENTERTAINMENT("Entertainment", "entertainment"),
    SCIENCE("Science", "science"),
    HEALTH("Health", "health"),
    WORLD_NEWS("World News", "world"),
    INDIA("India", "india"),
    OPINION("Opinion", "opinion"),
    ENVIRONMENT("Environment", "environment"),
    EDUCATION("Education", "education");

    companion object {
        fun fromString(value: String): TopicCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: WORLD_NEWS
        }
    }
}
