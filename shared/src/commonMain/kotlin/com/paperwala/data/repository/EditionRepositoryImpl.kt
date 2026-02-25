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

import com.paperwala.data.local.db.GetEditionArticles
import com.paperwala.data.local.db.PaperwalaDatabase
import com.paperwala.data.mapper.articleFromDbRow
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.Edition
import com.paperwala.domain.model.Section
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.repository.EditionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EditionRepositoryImpl(
    private val database: PaperwalaDatabase
) : EditionRepository {

    override fun getTodayEdition(): Edition? {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()

        val editionEntity = database.editionQueries.getEditionByDate(today)
            .executeAsOneOrNull() ?: return null

        val editionArticles = database.editionQueries.getEditionArticles(editionEntity.id)
            .executeAsList()

        return Edition(
            id = editionEntity.id,
            date = LocalDate.parse(editionEntity.date),
            generatedAt = Instant.fromEpochMilliseconds(editionEntity.generated_at),
            headline = editionEntity.headline,
            sections = buildSections(editionArticles),
            totalReadTimeMinutes = editionEntity.total_read_time_minutes.toInt(),
            articleCount = editionEntity.article_count.toInt(),
            isRead = editionEntity.is_read == 1L
        )
    }

    override fun saveEdition(edition: Edition) {
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

    override fun getRecentEditions(limit: Int): List<Edition> {
        val editions = database.editionQueries.getRecentEditions(limit.toLong())
            .executeAsList()

        return editions.map { entity ->
            val articles = database.editionQueries.getEditionArticles(entity.id)
                .executeAsList()

            Edition(
                id = entity.id,
                date = LocalDate.parse(entity.date),
                generatedAt = Instant.fromEpochMilliseconds(entity.generated_at),
                headline = entity.headline,
                sections = buildSections(articles),
                totalReadTimeMinutes = entity.total_read_time_minutes.toInt(),
                articleCount = entity.article_count.toInt(),
                isRead = entity.is_read == 1L
            )
        }
    }

    override fun markEditionAsRead(editionId: String) {
        database.editionQueries.markEditionAsRead(editionId)
    }

    private fun buildSections(rows: List<GetEditionArticles>): List<Section> {
        val sectionMap = mutableMapOf<String, MutableList<Article>>()
        for (row in rows) {
            val article = articleFromDbRow(
                id = row.id,
                title = row.title,
                summary = row.summary,
                fullContent = row.full_content,
                sourceUrl = row.source_url,
                sourceName = row.source_name,
                sourceLogoUrl = row.source_logo_url,
                imageUrl = row.image_url,
                author = row.author,
                publishedAt = row.published_at,
                fetchedAt = row.fetched_at,
                category = row.category,
                relevanceScore = row.relevance_score,
                readTimeMinutes = row.read_time_minutes,
                isRead = row.is_read,
                isBookmarked = row.is_bookmarked
            )
            sectionMap.getOrPut(row.section_category) { mutableListOf() }.add(article)
        }
        return sectionMap.map { (categoryName, articles) ->
            val category = TopicCategory.fromString(categoryName)
            Section(category = category, displayName = category.displayName, articles = articles)
        }
    }
}
