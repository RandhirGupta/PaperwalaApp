package com.paperwala.data.mapper

import com.paperwala.data.remote.dto.GNewsArticle
import com.paperwala.data.remote.dto.NewsApiArticle
import com.paperwala.data.remote.dto.RssItem
import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import com.paperwala.util.HtmlParser
import com.paperwala.util.ReadTimeCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ArticleMapper {

    fun fromNewsApi(dto: NewsApiArticle, category: TopicCategory? = null): Article {
        val parsedInstant = try {
            Instant.parse(dto.publishedAt)
        } catch (e: Exception) {
            Clock.System.now()
        }

        return Article(
            id = dto.url.hashCode().toString(),
            title = HtmlParser.stripTags(dto.title),
            summary = HtmlParser.stripTags(dto.description),
            fullContent = dto.content,
            sourceUrl = dto.url,
            sourceName = dto.source.name,
            imageUrl = dto.urlToImage,
            author = dto.author,
            publishedAt = parsedInstant,
            fetchedAt = Clock.System.now(),
            category = category ?: inferCategoryFromText(dto.title, dto.description),
            readTimeMinutes = ReadTimeCalculator.estimateMinutes(dto.content)
        )
    }

    fun fromGNews(dto: GNewsArticle, category: TopicCategory? = null): Article {
        val parsedInstant = try {
            Instant.parse(dto.publishedAt)
        } catch (e: Exception) {
            Clock.System.now()
        }

        return Article(
            id = dto.url.hashCode().toString(),
            title = HtmlParser.stripTags(dto.title),
            summary = HtmlParser.stripTags(dto.description),
            fullContent = dto.content,
            sourceUrl = dto.url,
            sourceName = dto.source.name,
            imageUrl = dto.image,
            publishedAt = parsedInstant,
            fetchedAt = Clock.System.now(),
            category = category ?: inferCategoryFromText(dto.title, dto.description),
            readTimeMinutes = ReadTimeCalculator.estimateMinutes(dto.content)
        )
    }

    fun fromRssItem(item: RssItem, sourceName: String): Article {
        val imageUrl = item.enclosureUrl ?: HtmlParser.extractFirstImageUrl(item.description)
        val cleanDescription = HtmlParser.stripTags(item.description)

        return Article(
            id = (item.link ?: item.guid ?: item.title).hashCode().toString(),
            title = HtmlParser.stripTags(item.title),
            summary = cleanDescription,
            sourceUrl = item.link ?: "",
            sourceName = sourceName,
            imageUrl = imageUrl,
            author = item.author,
            publishedAt = parseRssDate(item.pubDate),
            fetchedAt = Clock.System.now(),
            category = inferCategoryFromFeedUrl(item.feedUrl),
            readTimeMinutes = ReadTimeCalculator.estimateMinutes(cleanDescription)
        )
    }

    private fun parseRssDate(dateStr: String?): Instant {
        if (dateStr.isNullOrBlank()) return Clock.System.now()
        return try {
            Instant.parse(dateStr)
        } catch (e: Exception) {
            Clock.System.now()
        }
    }

    private fun inferCategoryFromFeedUrl(url: String): TopicCategory {
        val lowerUrl = url.lowercase()
        return when {
            "technology" in lowerUrl || "tech" in lowerUrl || "sci-tech" in lowerUrl -> TopicCategory.TECHNOLOGY
            "sport" in lowerUrl || "cricket" in lowerUrl -> TopicCategory.SPORTS
            "business" in lowerUrl || "market" in lowerUrl || "economy" in lowerUrl -> TopicCategory.BUSINESS
            "entertainment" in lowerUrl || "bollywood" in lowerUrl -> TopicCategory.ENTERTAINMENT
            "science" in lowerUrl -> TopicCategory.SCIENCE
            "health" in lowerUrl -> TopicCategory.HEALTH
            "opinion" in lowerUrl || "editorial" in lowerUrl -> TopicCategory.OPINION
            "environment" in lowerUrl || "climate" in lowerUrl -> TopicCategory.ENVIRONMENT
            "education" in lowerUrl -> TopicCategory.EDUCATION
            "world" in lowerUrl || "international" in lowerUrl -> TopicCategory.WORLD_NEWS
            "india" in lowerUrl || "national" in lowerUrl -> TopicCategory.INDIA
            "politics" in lowerUrl || "election" in lowerUrl -> TopicCategory.POLITICS
            else -> TopicCategory.INDIA
        }
    }

    private fun inferCategoryFromText(title: String?, description: String?): TopicCategory {
        val text = "${title ?: ""} ${description ?: ""}".lowercase()
        return when {
            Regex("\\b(tech|ai|software|startup|google|apple|microsoft|coding|app)\\b").containsMatchIn(text) -> TopicCategory.TECHNOLOGY
            Regex("\\b(cricket|football|ipl|sports|match|tournament|olympic)\\b").containsMatchIn(text) -> TopicCategory.SPORTS
            Regex("\\b(market|stock|sensex|nifty|gdp|economy|business|company|revenue)\\b").containsMatchIn(text) -> TopicCategory.BUSINESS
            Regex("\\b(movie|bollywood|celebrity|entertainment|film|album|music)\\b").containsMatchIn(text) -> TopicCategory.ENTERTAINMENT
            Regex("\\b(science|research|study|discover|nasa|space|physics)\\b").containsMatchIn(text) -> TopicCategory.SCIENCE
            Regex("\\b(health|medical|doctor|hospital|disease|vaccine|mental)\\b").containsMatchIn(text) -> TopicCategory.HEALTH
            Regex("\\b(election|parliament|modi|congress|bjp|government|minister|policy)\\b").containsMatchIn(text) -> TopicCategory.POLITICS
            Regex("\\b(climate|environment|pollution|forest|wildlife|carbon)\\b").containsMatchIn(text) -> TopicCategory.ENVIRONMENT
            Regex("\\b(education|school|university|exam|student|college)\\b").containsMatchIn(text) -> TopicCategory.EDUCATION
            Regex("\\b(india|delhi|mumbai|chennai|bengaluru|kolkata|hyderabad)\\b").containsMatchIn(text) -> TopicCategory.INDIA
            else -> TopicCategory.WORLD_NEWS
        }
    }

    fun deduplicateArticles(articles: List<Article>): List<Article> {
        val seen = mutableSetOf<String>()
        return articles.filter { article ->
            val key = article.sourceUrl.hashCode().toString()
            if (key in seen) {
                false
            } else {
                seen.add(key)
                // Also check for very similar titles
                val titleKey = article.title.lowercase().take(60)
                if (titleKey in seen) {
                    false
                } else {
                    seen.add(titleKey)
                    true
                }
            }
        }
    }
}
