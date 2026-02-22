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
package com.paperwala.data.remote.api

import com.paperwala.data.remote.dto.NewsApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NewsApiService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2"
    }

    suspend fun getTopHeadlines(
        apiKey: String,
        country: String = "in",
        category: String? = null,
        pageSize: Int = 20
    ): NewsApiResponse {
        return httpClient.get("$BASE_URL/top-headlines") {
            parameter("apiKey", apiKey)
            parameter("country", country)
            category?.let { parameter("category", it) }
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun searchNews(
        apiKey: String,
        query: String,
        language: String = "en",
        sortBy: String = "publishedAt",
        pageSize: Int = 20
    ): NewsApiResponse {
        return httpClient.get("$BASE_URL/everything") {
            parameter("apiKey", apiKey)
            parameter("q", query)
            parameter("language", language)
            parameter("sortBy", sortBy)
            parameter("pageSize", pageSize)
        }.body()
    }
}
