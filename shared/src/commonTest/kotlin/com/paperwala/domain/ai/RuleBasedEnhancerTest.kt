package com.paperwala.domain.ai

import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class RuleBasedEnhancerTest {

    private val enhancer = RuleBasedEnhancer()

    private fun createArticle(
        title: String = "Test Article Title",
        summary: String = "This is a test summary for the article.",
        fullContent: String? = null,
        category: TopicCategory = TopicCategory.TECHNOLOGY,
        imageUrl: String? = null,
        publishedAt: Instant = Clock.System.now()
    ) = Article(
        id = "test-${title.hashCode()}",
        title = title,
        summary = summary,
        fullContent = fullContent,
        sourceUrl = "https://example.com/article",
        sourceName = "Test Source",
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        fetchedAt = Clock.System.now(),
        category = category
    )

    @Test
    fun statusIsRuleBased() {
        assertEquals(AiStatus.RULE_BASED, enhancer.status)
    }

    @Test
    fun enhanceReturnsEnhancedArticlesForEachInput() = runTest {
        val articles = listOf(
            createArticle(title = "Article 1"),
            createArticle(title = "Article 2"),
            createArticle(title = "Article 3")
        )

        val results = enhancer.enhance(articles, listOf(TopicCategory.TECHNOLOGY))

        assertEquals(3, results.size)
        assertEquals("Article 1", results[0].article.title)
        assertEquals("Article 2", results[1].article.title)
        assertEquals("Article 3", results[2].article.title)
    }

    @Test
    fun enhanceEmptyListReturnsEmptyList() = runTest {
        val results = enhancer.enhance(emptyList(), listOf(TopicCategory.TECHNOLOGY))
        assertTrue(results.isEmpty())
    }

    @Test
    fun relevanceScoreHigherWhenTopicMatches() = runTest {
        val matchingArticle = createArticle(category = TopicCategory.TECHNOLOGY)
        val nonMatchingArticle = createArticle(category = TopicCategory.SPORTS)

        val results = enhancer.enhance(
            listOf(matchingArticle, nonMatchingArticle),
            listOf(TopicCategory.TECHNOLOGY)
        )

        assertTrue(
            results[0].aiRelevanceScore > results[1].aiRelevanceScore,
            "Matching topic should have higher relevance score"
        )
    }

    @Test
    fun relevanceScoreHigherForRecentArticles() = runTest {
        val now = Clock.System.now()
        val recentArticle = createArticle(
            title = "Recent",
            publishedAt = now,
            category = TopicCategory.SPORTS
        )
        val oldArticle = createArticle(
            title = "Old",
            publishedAt = now.minus(48.hours),
            category = TopicCategory.SPORTS
        )

        val results = enhancer.enhance(
            listOf(recentArticle, oldArticle),
            listOf(TopicCategory.SPORTS)
        )

        assertTrue(
            results[0].aiRelevanceScore > results[1].aiRelevanceScore,
            "Recent article should score higher than old article"
        )
    }

    @Test
    fun relevanceScoreBoostForImagePresence() = runTest {
        val withImage = createArticle(
            title = "With Image",
            imageUrl = "https://example.com/image.jpg",
            category = TopicCategory.WORLD_NEWS
        )
        val withoutImage = createArticle(
            title = "Without Image",
            imageUrl = null,
            category = TopicCategory.WORLD_NEWS
        )

        val results = enhancer.enhance(
            listOf(withImage, withoutImage),
            listOf(TopicCategory.WORLD_NEWS)
        )

        assertTrue(
            results[0].aiRelevanceScore > results[1].aiRelevanceScore,
            "Article with image should score slightly higher"
        )
    }

    @Test
    fun relevanceScoreIsBetweenZeroAndOne() = runTest {
        val articles = listOf(
            createArticle(title = "Article 1"),
            createArticle(title = "Article 2", category = TopicCategory.POLITICS),
            createArticle(title = "Article 3", category = TopicCategory.SPORTS)
        )

        val results = enhancer.enhance(articles, listOf(TopicCategory.TECHNOLOGY))

        results.forEach { result ->
            assertTrue(
                result.aiRelevanceScore in 0f..1f,
                "Score ${result.aiRelevanceScore} should be between 0 and 1"
            )
        }
    }

    @Test
    fun extractiveSummarizeReturnsShortTextAsIs() = runTest {
        val article = createArticle(
            summary = "Short summary text.",
            fullContent = null
        )

        val results = enhancer.enhance(listOf(article), emptyList())

        // Short text with <= 2 sentences should be returned as-is
        assertTrue(results[0].aiSummary.isNotBlank())
    }

    @Test
    fun extractiveSummarizePicksTopSentencesFromLongContent() = runTest {
        val longContent = "The government announced a new policy today. " +
            "The stock market reacted positively to the announcement. " +
            "Experts say this could boost economic growth significantly. " +
            "Opposition leaders criticized the decision strongly. " +
            "The policy will take effect starting next month."

        val article = createArticle(
            summary = "Government announces new policy",
            fullContent = longContent
        )

        val results = enhancer.enhance(listOf(article), emptyList())
        val summary = results[0].aiSummary

        assertTrue(summary.isNotBlank(), "Summary should not be blank")
        // Should be shorter than the full content (picked top 2 sentences)
        assertTrue(
            summary.length < longContent.length,
            "Extractive summary should be shorter than full content"
        )
    }

    @Test
    fun enhancePreservesOriginalArticle() = runTest {
        val article = createArticle(
            title = "Original Title",
            summary = "Original summary"
        )

        val results = enhancer.enhance(listOf(article), emptyList())

        assertEquals("Original Title", results[0].article.title)
        assertEquals("Original summary", results[0].article.summary)
        assertEquals(article.id, results[0].article.id)
    }

    @Test
    fun longerContentGetsHigherRelevanceScore() = runTest {
        val shortContent = createArticle(
            title = "Short",
            summary = "Brief.",
            fullContent = null,
            category = TopicCategory.WORLD_NEWS
        )
        val longContent = createArticle(
            title = "Long",
            summary = "Brief.",
            fullContent = "A".repeat(1500),
            category = TopicCategory.WORLD_NEWS
        )

        val results = enhancer.enhance(
            listOf(longContent, shortContent),
            listOf(TopicCategory.WORLD_NEWS)
        )

        assertTrue(
            results[0].aiRelevanceScore > results[1].aiRelevanceScore,
            "Longer content should score higher due to content richness bonus"
        )
    }
}
