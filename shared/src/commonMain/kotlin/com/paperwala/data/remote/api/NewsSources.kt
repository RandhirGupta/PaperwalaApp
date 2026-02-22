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

object NewsSources {

    data class SourceInfo(
        val id: String,
        val displayName: String,
        val isIndian: Boolean,
        val rssFeeds: Map<String, String> = emptyMap() // category -> url
    )

    val INDIAN_SOURCES = listOf(
        SourceInfo(
            id = "the-hindu",
            displayName = "The Hindu",
            isIndian = true,
            rssFeeds = mapOf(
                "national" to "https://www.thehindu.com/news/national/feeder/default.rss",
                "business" to "https://www.thehindu.com/business/feeder/default.rss",
                "sports" to "https://www.thehindu.com/sport/feeder/default.rss",
                "technology" to "https://www.thehindu.com/sci-tech/technology/feeder/default.rss",
                "opinion" to "https://www.thehindu.com/opinion/feeder/default.rss"
            )
        ),
        SourceInfo(
            id = "indian-express",
            displayName = "Indian Express",
            isIndian = true,
            rssFeeds = mapOf(
                "india" to "https://indianexpress.com/section/india/feed/",
                "business" to "https://indianexpress.com/section/business/feed/",
                "technology" to "https://indianexpress.com/section/technology/feed/",
                "sports" to "https://indianexpress.com/section/sports/feed/",
                "entertainment" to "https://indianexpress.com/section/entertainment/feed/"
            )
        ),
        SourceInfo(
            id = "hindustan-times",
            displayName = "Hindustan Times",
            isIndian = true,
            rssFeeds = mapOf(
                "india" to "https://www.hindustantimes.com/feeds/rss/india-news/rssfeed.xml",
                "business" to "https://www.hindustantimes.com/feeds/rss/business/rssfeed.xml",
                "sports" to "https://www.hindustantimes.com/feeds/rss/cricket/rssfeed.xml",
                "entertainment" to "https://www.hindustantimes.com/feeds/rss/entertainment/rssfeed.xml"
            )
        ),
        SourceInfo(
            id = "mint",
            displayName = "Mint",
            isIndian = true,
            rssFeeds = mapOf(
                "news" to "https://www.livemint.com/rss/news",
                "markets" to "https://www.livemint.com/rss/markets",
                "technology" to "https://www.livemint.com/rss/technology"
            )
        ),
        SourceInfo(
            id = "ndtv",
            displayName = "NDTV",
            isIndian = true,
            rssFeeds = mapOf(
                "india" to "https://feeds.feedburner.com/ndtvnews-india-news",
                "top-stories" to "https://feeds.feedburner.com/ndtvnews-top-stories"
            )
        ),
        SourceInfo(
            id = "times-of-india",
            displayName = "Times of India",
            isIndian = true,
            rssFeeds = mapOf(
                "india" to "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                "technology" to "https://timesofindia.indiatimes.com/rssfeeds/66949542.cms",
                "sports" to "https://timesofindia.indiatimes.com/rssfeeds/4719148.cms"
            )
        ),
        SourceInfo(
            id = "economic-times",
            displayName = "Economic Times",
            isIndian = true,
            rssFeeds = mapOf(
                "markets" to "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms",
                "tech" to "https://economictimes.indiatimes.com/tech/rssfeeds/13357270.cms"
            )
        ),
        SourceInfo(
            id = "the-wire",
            displayName = "The Wire",
            isIndian = true,
            rssFeeds = mapOf(
                "all" to "https://thewire.in/feed"
            )
        ),
        SourceInfo(
            id = "the-print",
            displayName = "The Print",
            isIndian = true,
            rssFeeds = mapOf(
                "all" to "https://theprint.in/feed/"
            )
        )
    )

    val INTERNATIONAL_SOURCES = listOf(
        SourceInfo(
            id = "bbc",
            displayName = "BBC",
            isIndian = false,
            rssFeeds = mapOf(
                "top" to "http://feeds.bbci.co.uk/news/rss.xml",
                "world" to "http://feeds.bbci.co.uk/news/world/rss.xml",
                "technology" to "http://feeds.bbci.co.uk/news/technology/rss.xml"
            )
        ),
        SourceInfo(
            id = "reuters",
            displayName = "Reuters",
            isIndian = false,
            rssFeeds = mapOf(
                "world" to "https://www.reutersagency.com/feed/"
            )
        ),
        SourceInfo(
            id = "the-guardian",
            displayName = "The Guardian",
            isIndian = false,
            rssFeeds = mapOf(
                "world" to "https://www.theguardian.com/world/rss",
                "technology" to "https://www.theguardian.com/uk/technology/rss"
            )
        ),
        SourceInfo(
            id = "al-jazeera",
            displayName = "Al Jazeera",
            isIndian = false,
            rssFeeds = mapOf(
                "all" to "https://www.aljazeera.com/xml/rss/all.xml"
            )
        ),
        SourceInfo(
            id = "ap-news",
            displayName = "AP News",
            isIndian = false,
            rssFeeds = mapOf(
                "top" to "https://rsshub.app/apnews/topics/apf-topnews"
            )
        )
    )

    val ALL_SOURCES = INDIAN_SOURCES + INTERNATIONAL_SOURCES

    fun getSourceById(id: String): SourceInfo? = ALL_SOURCES.find { it.id == id }
}
