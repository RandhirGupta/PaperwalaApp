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

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import com.paperwala.data.remote.dto.RssItem
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class RssFeedService(private val httpClient: HttpClient) : RssFeedApi {

    override suspend fun fetchFeed(url: String): List<RssItem> {
        return try {
            val xml = httpClient.get(url).bodyAsText()
            val items = parseWithKSoup(xml, feedUrl = url)
            println("[RssFeedService] Fetched ${items.size} items from $url")
            items
        } catch (e: Exception) {
            println("[RssFeedService] Failed to fetch $url: ${e::class.simpleName} - ${e.message}")
            emptyList()
        }
    }

    private fun parseWithKSoup(xml: String, feedUrl: String): List<RssItem> {
        val doc = Ksoup.parse(xml, parser = Parser.xmlParser())

        // Support both RSS 2.0 (<item>) and Atom (<entry>)
        val items = doc.select("item").ifEmpty { doc.select("entry") }

        return items.map { element ->
            val link = element.selectFirst("link")?.text()?.takeIf { it.isNotBlank() }
                ?: element.selectFirst("link")?.attr("href")

            val imageUrl = element.selectFirst("enclosure")?.attr("url")
                ?: element.selectFirst("media|content")?.attr("url")
                ?: element.selectFirst("media|thumbnail")?.attr("url")

            RssItem(
                title = element.selectFirst("title")?.text() ?: "",
                link = link,
                description = element.selectFirst("description")?.text()
                    ?: element.selectFirst("summary")?.text(),
                author = element.selectFirst("dc|creator")?.text()
                    ?: element.selectFirst("author")?.text()
                    ?: element.selectFirst("author name")?.text(),
                pubDate = element.selectFirst("pubDate")?.text()
                    ?: element.selectFirst("published")?.text()
                    ?: element.selectFirst("updated")?.text(),
                guid = element.selectFirst("guid")?.text()
                    ?: element.selectFirst("id")?.text(),
                enclosureUrl = imageUrl,
                feedUrl = feedUrl
            )
        }
    }
}
