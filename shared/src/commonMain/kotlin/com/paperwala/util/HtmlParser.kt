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
package com.paperwala.util

object HtmlParser {

    fun stripTags(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return html
            .replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("&lt;"), "<")
            .replace(Regex("&gt;"), ">")
            .replace(Regex("&quot;"), "\"")
            .replace(Regex("&apos;"), "'")
            .replace(Regex("&#\\d+;")) { matchResult ->
                val code = matchResult.value.drop(2).dropLast(1).toIntOrNull()
                code?.toChar()?.toString() ?: matchResult.value
            }
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun extractFirstImageUrl(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val regex = Regex("<img[^>]+src=\"([^\"]+)\"")
        return regex.find(html)?.groupValues?.get(1)
    }
}
