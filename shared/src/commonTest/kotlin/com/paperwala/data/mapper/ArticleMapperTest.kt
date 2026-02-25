package com.paperwala.data.mapper

import com.paperwala.data.remote.dto.GNewsArticle
import com.paperwala.data.remote.dto.GNewsSource
import com.paperwala.data.remote.dto.NewsApiArticle
import com.paperwala.data.remote.dto.NewsApiSource
import com.paperwala.data.remote.dto.RssItem
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArticleMapperTest {

    // --- fromNewsApi ---

    @Test
    fun fromNewsApiMapsBasicFields() {
        val dto = NewsApiArticle(
            source = NewsApiSource(name = "BBC"),
            title = "Test Title",
            description = "Test description",
            url = "https://bbc.com/article",
            publishedAt = "2026-02-23T10:00:00Z"
        )

        val article = ArticleMapper.fromNewsApi(dto)

        assertEquals("Test Title", article.title)
        assertEquals("Test description", article.summary)
        assertEquals("https://bbc.com/article", article.sourceUrl)
        assertEquals("BBC", article.sourceName)
    }

    @Test
    fun fromNewsApiUsesProvidedCategory() {
        val dto = NewsApiArticle(
            url = "https://example.com",
            title = "Article",
            publishedAt = "2026-02-23T10:00:00Z"
        )

        val article = ArticleMapper.fromNewsApi(dto, TopicCategory.SPORTS)
        assertEquals(TopicCategory.SPORTS, article.category)
    }

    @Test
    fun fromNewsApiInfersCategoryFromTitle() {
        val dto = NewsApiArticle(
            url = "https://example.com",
            title = "Google launches new AI software for developers",
            publishedAt = "2026-02-23T10:00:00Z"
        )

        val article = ArticleMapper.fromNewsApi(dto)
        assertEquals(TopicCategory.TECHNOLOGY, article.category)
    }

    @Test
    fun fromNewsApiStripsHtmlFromTitle() {
        val dto = NewsApiArticle(
            url = "https://example.com",
            title = "<b>Bold</b> Title",
            publishedAt = "2026-02-23T10:00:00Z"
        )

        val article = ArticleMapper.fromNewsApi(dto)
        assertEquals("Bold Title", article.title)
    }

    @Test
    fun fromNewsApiHandlesInvalidDate() {
        val dto = NewsApiArticle(
            url = "https://example.com",
            title = "Article",
            publishedAt = "not-a-date"
        )

        // Should not throw, falls back to Clock.System.now()
        val article = ArticleMapper.fromNewsApi(dto)
        assertNotNull(article.publishedAt)
    }

    @Test
    fun fromNewsApiGeneratesIdFromUrl() {
        val dto = NewsApiArticle(url = "https://example.com/123", title = "Title", publishedAt = "2026-02-23T10:00:00Z")
        val article = ArticleMapper.fromNewsApi(dto)
        assertEquals("https://example.com/123".hashCode().toString(), article.id)
    }

    // --- fromGNews ---

    @Test
    fun fromGNewsMapsBasicFields() {
        val dto = GNewsArticle(
            title = "GNews Article",
            description = "GNews summary",
            url = "https://gnews.com/article",
            image = "https://gnews.com/image.jpg",
            publishedAt = "2026-02-23T10:00:00Z",
            source = GNewsSource(name = "Reuters")
        )

        val article = ArticleMapper.fromGNews(dto)

        assertEquals("GNews Article", article.title)
        assertEquals("GNews summary", article.summary)
        assertEquals("Reuters", article.sourceName)
        assertEquals("https://gnews.com/image.jpg", article.imageUrl)
    }

    @Test
    fun fromGNewsUsesProvidedCategory() {
        val dto = GNewsArticle(
            url = "https://example.com",
            title = "Article",
            publishedAt = "2026-02-23T10:00:00Z"
        )

        val article = ArticleMapper.fromGNews(dto, TopicCategory.BUSINESS)
        assertEquals(TopicCategory.BUSINESS, article.category)
    }

    // --- fromRssItem ---

    @Test
    fun fromRssItemMapsBasicFields() {
        val item = RssItem(
            title = "RSS Article",
            link = "https://thehindu.com/rss-article",
            description = "RSS description",
            author = "Author Name",
            feedUrl = "https://thehindu.com/technology/rss"
        )

        val article = ArticleMapper.fromRssItem(item, "The Hindu")

        assertEquals("RSS Article", article.title)
        assertEquals("RSS description", article.summary)
        assertEquals("The Hindu", article.sourceName)
        assertEquals("Author Name", article.author)
    }

    @Test
    fun fromRssItemInfersCategoryFromFeedUrl() {
        val techItem = RssItem(title = "Article", feedUrl = "https://example.com/technology/rss")
        val sportsItem = RssItem(title = "Article", feedUrl = "https://example.com/sport/rss")
        val businessItem = RssItem(title = "Article", feedUrl = "https://example.com/business/rss")

        assertEquals(TopicCategory.TECHNOLOGY, ArticleMapper.fromRssItem(techItem, "Src").category)
        assertEquals(TopicCategory.SPORTS, ArticleMapper.fromRssItem(sportsItem, "Src").category)
        assertEquals(TopicCategory.BUSINESS, ArticleMapper.fromRssItem(businessItem, "Src").category)
    }

    @Test
    fun fromRssItemExtractsImageFromEnclosure() {
        val item = RssItem(
            title = "Article",
            enclosureUrl = "https://example.com/enclosure.jpg",
            description = "<img src=\"https://example.com/inline.jpg\"/>"
        )

        val article = ArticleMapper.fromRssItem(item, "Src")
        assertEquals("https://example.com/enclosure.jpg", article.imageUrl)
    }

    @Test
    fun fromRssItemExtractsImageFromHtmlWhenNoEnclosure() {
        val item = RssItem(
            title = "Article",
            enclosureUrl = null,
            description = "<p>Text</p><img src=\"https://example.com/img.jpg\"/>"
        )

        val article = ArticleMapper.fromRssItem(item, "Src")
        assertEquals("https://example.com/img.jpg", article.imageUrl)
    }

    // --- deduplicateArticles ---

    @Test
    fun deduplicateRemovesDuplicateUrls() {
        val articles = listOf(
            createArticle("1", "Title A", "https://example.com/1"),
            createArticle("2", "Title B", "https://example.com/1"), // same URL
            createArticle("3", "Title C", "https://example.com/2")
        )

        val deduped = ArticleMapper.deduplicateArticles(articles)

        assertEquals(2, deduped.size)
        assertEquals("1", deduped[0].id)
        assertEquals("3", deduped[1].id)
    }

    @Test
    fun deduplicateRemovesSimilarTitles() {
        // Titles share the same first 60 lowercase characters so dedup treats them as similar
        val longSharedPrefix = "India wins the cricket world cup in a dramatic final at lords" // exactly 61 chars
        val articles = listOf(
            createArticle("1", longSharedPrefix + " stadium", "https://a.com"),
            createArticle("2", longSharedPrefix + " ground today", "https://b.com"),
            createArticle("3", "Completely different article about technology and innovation", "https://c.com")
        )

        val deduped = ArticleMapper.deduplicateArticles(articles)

        assertEquals(2, deduped.size)
        assertEquals("1", deduped[0].id)
        assertEquals("3", deduped[1].id)
    }

    @Test
    fun deduplicatePreservesOrderOfFirstOccurrence() {
        val articles = listOf(
            createArticle("a", "First unique article title here", "https://1.com"),
            createArticle("b", "Second unique article title here", "https://2.com"),
            createArticle("c", "Third unique article title here", "https://3.com")
        )

        val deduped = ArticleMapper.deduplicateArticles(articles)

        assertEquals(3, deduped.size)
        assertEquals("a", deduped[0].id)
        assertEquals("b", deduped[1].id)
        assertEquals("c", deduped[2].id)
    }

    @Test
    fun deduplicateHandlesEmptyList() {
        assertTrue(ArticleMapper.deduplicateArticles(emptyList()).isEmpty())
    }

    private fun createArticle(id: String, title: String, url: String) = Article(
        id = id,
        title = title,
        summary = "summary",
        sourceUrl = url,
        sourceName = "Source",
        publishedAt = kotlinx.datetime.Clock.System.now(),
        fetchedAt = kotlinx.datetime.Clock.System.now(),
        category = TopicCategory.WORLD_NEWS
    )
}
