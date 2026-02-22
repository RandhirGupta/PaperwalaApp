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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsdataResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val results: List<NewsdataArticle> = emptyList(),
    val nextPage: String? = null
)

@Serializable
data class NewsdataArticle(
    @SerialName("article_id") val articleId: String = "",
    val title: String = "",
    val link: String = "",
    val description: String? = null,
    val content: String? = null,
    val pubDate: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("source_id") val sourceId: String = "",
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val creator: List<String>? = null,
    val category: List<String>? = null,
    val country: List<String>? = null,
    val language: String? = null
)
