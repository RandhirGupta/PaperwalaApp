package com.paperwala.domain.ai

import com.paperwala.data.remote.api.GeminiApiService
import com.paperwala.data.remote.dto.GeminiCandidate
import com.paperwala.data.remote.dto.GeminiContent
import com.paperwala.data.remote.dto.GeminiPart
import com.paperwala.data.remote.dto.GeminiResponse
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CloudArticleEnhancerTest {

    private fun createArticle(
        title: String = "Test Article",
        summary: String = "Test summary",
        category: TopicCategory = TopicCategory.TECHNOLOGY
    ) = Article(
        id = "test-${title.hashCode()}",
        title = title,
        summary = summary,
        sourceUrl = "https://example.com",
        sourceName = "Test",
        publishedAt = Clock.System.now(),
        fetchedAt = Clock.System.now(),
        category = category
    )

    @Test
    fun statusIsCloud() {
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(),
            apiKey = "test-key"
        )
        assertEquals(AiStatus.CLOUD, enhancer.status)
    }

    @Test
    fun blankApiKeyReturnsOriginalArticles() = runTest {
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(),
            apiKey = ""
        )

        val articles = listOf(createArticle(summary = "original"))
        val results = enhancer.enhance(articles, listOf(TopicCategory.TECHNOLOGY))

        assertEquals(1, results.size)
        assertEquals("original", results[0].aiSummary)
    }

    @Test
    fun emptyArticleListReturnsEmpty() = runTest {
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(),
            apiKey = "test-key"
        )

        val results = enhancer.enhance(emptyList(), listOf(TopicCategory.TECHNOLOGY))
        assertTrue(results.isEmpty())
    }

    @Test
    fun validResponseParsesCorrectly() = runTest {
        val responseJson = """[{"index":0,"summary":"AI generated summary","relevanceScore":85}]"""
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(responseText = responseJson),
            apiKey = "test-key"
        )

        val articles = listOf(createArticle())
        val results = enhancer.enhance(articles, listOf(TopicCategory.TECHNOLOGY))

        assertEquals(1, results.size)
        assertEquals("AI generated summary", results[0].aiSummary)
        assertEquals(0.85f, results[0].aiRelevanceScore)
    }

    @Test
    fun invalidJsonFallsBackToOriginalData() = runTest {
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(responseText = "not valid json"),
            apiKey = "test-key"
        )

        val article = createArticle(summary = "fallback summary")
        val results = enhancer.enhance(listOf(article), listOf(TopicCategory.TECHNOLOGY))

        assertEquals(1, results.size)
        assertEquals("fallback summary", results[0].aiSummary)
    }

    @Test
    fun apiExceptionFallsBackToOriginalData() = runTest {
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(shouldThrow = true),
            apiKey = "test-key"
        )

        val article = createArticle(summary = "safe summary")
        val results = enhancer.enhance(listOf(article), listOf(TopicCategory.TECHNOLOGY))

        assertEquals(1, results.size)
        assertEquals("safe summary", results[0].aiSummary)
    }

    @Test
    fun batchesArticlesInGroupsOfFive() = runTest {
        var callCount = 0
        val service = object : GeminiApiService(HttpClient()) {
            override suspend fun generateContent(
                apiKey: String,
                prompt: String,
                temperature: Float,
                maxOutputTokens: Int
            ): GeminiResponse {
                callCount++
                return GeminiResponse(
                    candidates = listOf(
                        GeminiCandidate(
                            content = GeminiContent(
                                parts = listOf(GeminiPart(text = "[]"))
                            )
                        )
                    )
                )
            }
        }

        val enhancer = CloudArticleEnhancer(geminiApiService = service, apiKey = "test-key")
        val articles = (1..12).map { createArticle(title = "Article $it") }

        enhancer.enhance(articles, listOf(TopicCategory.TECHNOLOGY))

        // 12 articles / 5 per batch = 3 API calls
        assertEquals(3, callCount, "Should batch 12 articles into 3 API calls")
    }

    @Test
    fun relevanceScoreNormalizedToZeroToOne() = runTest {
        val responseJson = """[{"index":0,"summary":"summary","relevanceScore":75}]"""
        val enhancer = CloudArticleEnhancer(
            geminiApiService = FakeGeminiApiService(responseText = responseJson),
            apiKey = "test-key"
        )

        val results = enhancer.enhance(
            listOf(createArticle()),
            listOf(TopicCategory.TECHNOLOGY)
        )

        // 75 / 100 = 0.75
        assertEquals(0.75f, results[0].aiRelevanceScore)
    }
}

/** Fake GeminiApiService for testing without network calls. */
private open class FakeGeminiApiService(
    private val responseText: String = "[]",
    private val shouldThrow: Boolean = false
) : GeminiApiService(HttpClient()) {

    override suspend fun generateContent(
        apiKey: String,
        prompt: String,
        temperature: Float,
        maxOutputTokens: Int
    ): GeminiResponse {
        if (shouldThrow) throw RuntimeException("API error")
        return GeminiResponse(
            candidates = listOf(
                GeminiCandidate(
                    content = GeminiContent(
                        parts = listOf(GeminiPart(text = responseText))
                    )
                )
            )
        )
    }
}
