package com.paperwala.data.remote.dto

data class RssItem(
    val title: String = "",
    val link: String? = null,
    val description: String? = null,
    val author: String? = null,
    val pubDate: String? = null,
    val guid: String? = null,
    val enclosureUrl: String? = null,
    val feedUrl: String = ""
)
