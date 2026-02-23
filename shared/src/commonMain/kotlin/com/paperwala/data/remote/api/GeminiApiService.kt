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

import com.paperwala.data.remote.dto.GeminiContent
import com.paperwala.data.remote.dto.GeminiGenerationConfig
import com.paperwala.data.remote.dto.GeminiPart
import com.paperwala.data.remote.dto.GeminiRequest
import com.paperwala.data.remote.dto.GeminiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

open class GeminiApiService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    }

    open suspend fun generateContent(
        apiKey: String,
        prompt: String,
        temperature: Float = 0.3f,
        maxOutputTokens: Int = 2048
    ): GeminiResponse {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                maxOutputTokens = maxOutputTokens,
                responseMimeType = "application/json"
            )
        )

        return httpClient.post(BASE_URL) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
