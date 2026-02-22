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
