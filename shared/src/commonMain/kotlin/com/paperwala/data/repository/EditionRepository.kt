package com.paperwala.data.repository

import com.paperwala.data.local.db.PaperwalaDatabase
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.Edition
import com.paperwala.domain.model.Section
import com.paperwala.domain.model.TopicCategory
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EditionRepository(
    private val database: PaperwalaDatabase
) {

    fun getTodayEdition(): Edition? {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()

        val editionEntity = database.editionQueries.getEditionByDate(today)
            .executeAsOneOrNull() ?: return null

        val editionArticles = database.editionQueries.getEditionArticles(editionEntity.id)
            .executeAsList()

        // Group articles by section
        val sectionMap = mutableMapOf<String, MutableList<Article>>()
        for (row in editionArticles) {
            val article = Article(
                id = row.id,
                title = row.title,
                summary = row.summary,
                fullContent = row.full_content,
                sourceUrl = row.source_url,
                sourceName = row.source_name,
                sourceLogoUrl = row.source_logo_url,
                imageUrl = row.image_url,
                author = row.author,
                publishedAt = Instant.fromEpochMilliseconds(row.published_at),
                fetchedAt = Instant.fromEpochMilliseconds(row.fetched_at),
                category = TopicCategory.fromString(row.category),
                relevanceScore = row.relevance_score.toFloat(),
                readTimeMinutes = row.read_time_minutes.toInt(),
                isRead = row.is_read == 1L,
                isBookmarked = row.is_bookmarked == 1L
            )
            sectionMap.getOrPut(row.section_category) { mutableListOf() }.add(article)
        }

        val sections = sectionMap.map { (categoryName, articles) ->
            val category = TopicCategory.fromString(categoryName)
            Section(
                category = category,
                displayName = category.displayName,
                articles = articles
            )
        }

        return Edition(
            id = editionEntity.id,
            date = LocalDate.parse(editionEntity.date),
            generatedAt = Instant.fromEpochMilliseconds(editionEntity.generated_at),
            headline = editionEntity.headline,
            sections = sections,
            totalReadTimeMinutes = editionEntity.total_read_time_minutes.toInt(),
            articleCount = editionEntity.article_count.toInt(),
            isRead = editionEntity.is_read == 1L
        )
    }

    fun saveEdition(edition: Edition) {
        database.editionQueries.insertEdition(
            id = edition.id,
            date = edition.date.toString(),
            generated_at = edition.generatedAt.toEpochMilliseconds(),
            headline = edition.headline,
            total_read_time_minutes = edition.totalReadTimeMinutes.toLong(),
            article_count = edition.articleCount.toLong(),
            is_read = if (edition.isRead) 1L else 0L
        )

        // Save section-article mappings
        var globalOrder = 0
        for (section in edition.sections) {
            for (article in section.articles) {
                database.editionQueries.insertEditionArticle(
                    edition_id = edition.id,
                    article_id = article.id,
                    section_category = section.category.name,
                    display_order = globalOrder.toLong()
                )
                globalOrder++
            }
        }
    }

    fun getRecentEditions(limit: Int): List<Edition> {
        val editions = database.editionQueries.getRecentEditions(limit.toLong())
            .executeAsList()

        return editions.map { entity ->
            val articles = database.editionQueries.getEditionArticles(entity.id)
                .executeAsList()

            val sectionMap = mutableMapOf<String, MutableList<Article>>()
            for (row in articles) {
                val article = Article(
                    id = row.id,
                    title = row.title,
                    summary = row.summary,
                    fullContent = row.full_content,
                    sourceUrl = row.source_url,
                    sourceName = row.source_name,
                    sourceLogoUrl = row.source_logo_url,
                    imageUrl = row.image_url,
                    author = row.author,
                    publishedAt = Instant.fromEpochMilliseconds(row.published_at),
                    fetchedAt = Instant.fromEpochMilliseconds(row.fetched_at),
                    category = TopicCategory.fromString(row.category),
                    relevanceScore = row.relevance_score.toFloat(),
                    readTimeMinutes = row.read_time_minutes.toInt(),
                    isRead = row.is_read == 1L,
                    isBookmarked = row.is_bookmarked == 1L
                )
                sectionMap.getOrPut(row.section_category) { mutableListOf() }.add(article)
            }

            Edition(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                generatedAt = Instant.fromEpochMilliseconds(entity.generated_at),
                headline = entity.headline,
                sections = sectionMap.map { (categoryName, articleList) ->
                    val category = TopicCategory.fromString(categoryName)
                    Section(category = category, displayName = category.displayName, articles = articleList)
                },
                totalReadTimeMinutes = entity.total_read_time_minutes.toInt(),
                articleCount = entity.article_count.toInt(),
                isRead = entity.is_read == 1L
            )
        }
    }

    fun markEditionAsRead(editionId: String) {
        database.editionQueries.markEditionAsRead(editionId)
    }
}
