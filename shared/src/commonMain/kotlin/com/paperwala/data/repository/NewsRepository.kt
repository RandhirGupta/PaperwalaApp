package com.paperwala.data.repository

import com.paperwala.data.local.db.ArticleEntity
import com.paperwala.data.local.db.PaperwalaDatabase
import com.paperwala.data.mapper.ArticleMapper
import com.paperwala.data.remote.api.GNewsApiService
import com.paperwala.data.remote.api.NewsApiService
import com.paperwala.data.remote.api.NewsSources
import com.paperwala.data.remote.api.RssFeedService
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock

class NewsRepository(
    private val database: PaperwalaDatabase,
    private val newsApiService: NewsApiService,
    private val gNewsApiService: GNewsApiService,
    private val rssFeedService: RssFeedService,
    private val newsApiKey: String = "",
    private val gNewsApiKey: String = ""
) {

    suspend fun fetchAndStoreNews(
        preferredSources: List<String> = emptyList()
    ): List<Article> = coroutineScope {
        val allArticles = mutableListOf<Article>()

        // Fetch from multiple sources in parallel
        val fetchJobs = mutableListOf<kotlinx.coroutines.Deferred<List<Article>>>()

        // NewsAPI
        if (newsApiKey.isNotBlank()) {
            fetchJobs.add(async { fetchFromNewsApi() })
        }

        // GNews
        if (gNewsApiKey.isNotBlank()) {
            fetchJobs.add(async { fetchFromGNews() })
        }

        // RSS feeds from preferred sources
        val rssSources = preferredSources.ifEmpty {
            NewsSources.INDIAN_SOURCES.map { it.id }
        }
        for (sourceId in rssSources) {
            val source = NewsSources.getSourceById(sourceId)
            if (source != null && source.rssFeeds.isNotEmpty()) {
                fetchJobs.add(async { fetchFromRss(source) })
            }
        }

        // Await all and combine
        val results = fetchJobs.awaitAll()
        results.forEach { allArticles.addAll(it) }

        // Deduplicate
        val deduplicated = ArticleMapper.deduplicateArticles(allArticles)

        // Store in database
        deduplicated.forEach { article ->
            database.articleQueries.insertArticle(
                id = article.id,
                title = article.title,
                summary = article.summary,
                full_content = article.fullContent,
                source_url = article.sourceUrl,
                source_name = article.sourceName,
                source_logo_url = article.sourceLogoUrl,
                image_url = article.imageUrl,
                author = article.author,
                published_at = article.publishedAt.toEpochMilliseconds(),
                fetched_at = article.fetchedAt.toEpochMilliseconds(),
                category = article.category.name,
                relevance_score = article.relevanceScore.toDouble(),
                read_time_minutes = article.readTimeMinutes.toLong(),
                is_read = if (article.isRead) 1L else 0L,
                is_bookmarked = if (article.isBookmarked) 1L else 0L
            )
        }

        // Clean old articles (older than 48 hours)
        val cutoff = Clock.System.now().toEpochMilliseconds() - (48 * 60 * 60 * 1000L)
        database.articleQueries.deleteOldArticles(cutoff)

        deduplicated
    }

    fun getRecentArticles(sinceMillis: Long): List<Article> {
        return database.articleQueries.getRecentArticles(sinceMillis)
            .executeAsList()
            .map { it.toDomain() }
    }

    fun getTopArticles(sinceMillis: Long, limit: Int): List<Article> {
        return database.articleQueries.getTopArticles(sinceMillis, limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }

    fun getArticlesByCategory(category: TopicCategory, limit: Int): List<Article> {
        return database.articleQueries.getArticlesByCategory(category.name, limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }

    fun markArticleAsRead(articleId: String) {
        database.articleQueries.markAsRead(articleId)
    }

    fun toggleBookmark(articleId: String) {
        database.articleQueries.toggleBookmark(articleId)
    }

    private suspend fun fetchFromNewsApi(): List<Article> {
        return try {
            val response = newsApiService.getTopHeadlines(apiKey = newsApiKey)
            response.articles.map { ArticleMapper.fromNewsApi(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchFromGNews(): List<Article> {
        return try {
            val response = gNewsApiService.getTopHeadlines(apiKey = gNewsApiKey)
            response.articles.map { ArticleMapper.fromGNews(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchFromRss(source: NewsSources.SourceInfo): List<Article> {
        val articles = mutableListOf<Article>()
        for ((_, feedUrl) in source.rssFeeds) {
            try {
                val items = rssFeedService.fetchFeed(feedUrl)
                articles.addAll(items.map { ArticleMapper.fromRssItem(it, source.displayName) })
            } catch (e: Exception) {
                // Skip failed feed, continue with others
            }
        }
        return articles
    }
}

private fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        summary = summary,
        fullContent = full_content,
        sourceUrl = source_url,
        sourceName = source_name,
        sourceLogoUrl = source_logo_url,
        imageUrl = image_url,
        author = author,
        publishedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(published_at),
        fetchedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(fetched_at),
        category = TopicCategory.fromString(category),
        relevanceScore = relevance_score.toFloat(),
        readTimeMinutes = read_time_minutes.toInt(),
        isRead = is_read == 1L,
        isBookmarked = is_bookmarked == 1L
    )
}
