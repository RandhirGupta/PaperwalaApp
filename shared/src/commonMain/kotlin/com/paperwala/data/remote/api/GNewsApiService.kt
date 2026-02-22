package com.paperwala.data.remote.api

import com.paperwala.data.remote.dto.GNewsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GNewsApiService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://gnews.io/api/v4"
    }

    suspend fun getTopHeadlines(
        apiKey: String,
        country: String = "in",
        category: String? = null,
        max: Int = 10
    ): GNewsResponse {
        return httpClient.get("$BASE_URL/top-headlines") {
            parameter("apikey", apiKey)
            parameter("country", country)
            category?.let { parameter("category", it) }
            parameter("max", max)
            parameter("lang", "en")
        }.body()
    }

    suspend fun searchNews(
        apiKey: String,
        query: String,
        max: Int = 10
    ): GNewsResponse {
        return httpClient.get("$BASE_URL/search") {
            parameter("apikey", apiKey)
            parameter("q", query)
            parameter("max", max)
            parameter("lang", "en")
            parameter("country", "in")
        }.body()
    }
}
