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
package com.paperwala.data.repository

import com.paperwala.data.local.db.PaperwalaDatabase
import com.paperwala.data.mapper.ArticleMapper
import com.paperwala.data.mapper.toDomain
import com.paperwala.data.remote.api.GNewsApi
import com.paperwala.data.remote.api.NewsApi
import com.paperwala.data.remote.api.NewsSources
import com.paperwala.data.remote.api.RssFeedApi
import com.paperwala.domain.exception.PaperwalaException
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.repository.NewsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock

class NewsRepositoryImpl(
    private val database: PaperwalaDatabase,
    private val newsApiService: NewsApi,
    private val gNewsApiService: GNewsApi,
    private val rssFeedService: RssFeedApi,
    private val articleMapper: ArticleMapper,
    private val newsApiKey: String = "",
    private val gNewsApiKey: String = ""
) : NewsRepository {

    override suspend fun fetchAndStoreNews(
        preferredSources: List<String>
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
        val deduplicated = articleMapper.deduplicateArticles(allArticles)

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

        println("[NewsRepository] fetchAndStoreNews complete: ${allArticles.size} raw, ${deduplicated.size} deduplicated")

        deduplicated
    }

    override fun getRecentArticles(sinceMillis: Long): List<Article> {
        return database.articleQueries.getRecentArticles(sinceMillis)
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun getTopArticles(sinceMillis: Long, limit: Int): List<Article> {
        return database.articleQueries.getTopArticles(sinceMillis, limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun getArticlesByCategory(category: TopicCategory, limit: Int): List<Article> {
        return database.articleQueries.getArticlesByCategory(category.name, limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun markArticleAsRead(articleId: String) {
        database.articleQueries.markAsRead(articleId)
        database.readingHistoryQueries.insertReadEvent(
            article_id = articleId,
            edition_id = null,
            read_at = Clock.System.now().toEpochMilliseconds(),
            read_duration_seconds = null
        )
    }

    override fun toggleBookmark(articleId: String) {
        database.articleQueries.toggleBookmark(articleId)
    }

    override fun getBookmarkedArticles(): List<Article> {
        return database.articleQueries.getBookmarkedArticles()
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun updateArticleSummaryAndScore(articleId: String, summary: String, relevanceScore: Float) {
        database.articleQueries.updateSummaryAndScore(summary, relevanceScore.toDouble(), articleId)
    }

    private suspend fun fetchFromNewsApi(): List<Article> {
        return try {
            val response = newsApiService.getTopHeadlines(apiKey = newsApiKey)
            response.articles.map { articleMapper.fromNewsApi(it) }
        } catch (e: PaperwalaException) {
            println("[NewsRepository] NewsAPI failed: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            println("[NewsRepository] NewsAPI failed: ${e::class.simpleName} - ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchFromGNews(): List<Article> {
        return try {
            val response = gNewsApiService.getTopHeadlines(apiKey = gNewsApiKey)
            response.articles.map { articleMapper.fromGNews(it) }
        } catch (e: PaperwalaException) {
            println("[NewsRepository] GNews failed: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            println("[NewsRepository] GNews failed: ${e::class.simpleName} - ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchFromRss(source: NewsSources.SourceInfo): List<Article> {
        val articles = mutableListOf<Article>()
        for ((category, feedUrl) in source.rssFeeds) {
            try {
                val items = rssFeedService.fetchFeed(feedUrl)
                val mapped = items.map { articleMapper.fromRssItem(it, source.displayName) }
                articles.addAll(mapped)
            } catch (e: PaperwalaException) {
                println("[NewsRepository] ${source.displayName}/$category FAILED: ${e.message}")
            } catch (e: Exception) {
                println("[NewsRepository] ${source.displayName}/$category FAILED: ${e::class.simpleName} - ${e.message}")
            }
        }
        println("[NewsRepository] Total from ${source.displayName}: ${articles.size} articles")
        return articles
    }
}
