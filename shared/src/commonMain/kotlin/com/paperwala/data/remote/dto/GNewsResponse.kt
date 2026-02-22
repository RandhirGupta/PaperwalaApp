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
package com.paperwala.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GNewsResponse(
    val totalArticles: Int = 0,
    val articles: List<GNewsArticle> = emptyList()
)

@Serializable
data class GNewsArticle(
    val title: String = "",
    val description: String? = null,
    val content: String? = null,
    val url: String = "",
    val image: String? = null,
    val publishedAt: String = "",
    val source: GNewsSource = GNewsSource()
)

@Serializable
data class GNewsSource(
    val name: String = "",
    val url: String = ""
)
