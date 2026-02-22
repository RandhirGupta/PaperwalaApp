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

import com.paperwala.data.remote.dto.RssItem
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class RssFeedService(private val httpClient: HttpClient) {

    suspend fun fetchFeed(url: String): List<RssItem> {
        return try {
            val xml = httpClient.get(url).bodyAsText()
            parseRssXml(xml, feedUrl = url)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRssXml(xml: String, feedUrl: String): List<RssItem> {
        val items = mutableListOf<RssItem>()
        val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
        val matches = itemRegex.findAll(xml)

        for (match in matches) {
            val itemXml = match.groupValues[1]
            items.add(
                RssItem(
                    title = extractTag(itemXml, "title"),
                    link = extractTagOrNull(itemXml, "link"),
                    description = extractTagOrNull(itemXml, "description"),
                    author = extractTagOrNull(itemXml, "dc:creator")
                        ?: extractTagOrNull(itemXml, "author"),
                    pubDate = extractTagOrNull(itemXml, "pubDate"),
                    guid = extractTagOrNull(itemXml, "guid"),
                    enclosureUrl = extractEnclosureUrl(itemXml),
                    feedUrl = feedUrl
                )
            )
        }
        return items
    }

    private fun extractTag(xml: String, tag: String): String {
        return extractTagOrNull(xml, tag) ?: ""
    }

    private fun extractTagOrNull(xml: String, tag: String): String? {
        // Handle CDATA sections
        val cdataRegex = Regex("<$tag[^>]*>\\s*<!\\[CDATA\\[(.*?)]]>\\s*</$tag>", RegexOption.DOT_MATCHES_ALL)
        cdataRegex.find(xml)?.let { return it.groupValues[1].trim() }

        // Handle regular tags
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.trim()
    }

    private fun extractEnclosureUrl(xml: String): String? {
        val regex = Regex("<enclosure[^>]*url=\"([^\"]+)\"")
        return regex.find(xml)?.groupValues?.get(1)
    }
}
