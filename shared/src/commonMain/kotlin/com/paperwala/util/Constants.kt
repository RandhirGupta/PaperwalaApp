package com.paperwala.util

object Constants {
    // API keys should be loaded from BuildConfig or secure storage
    // These are placeholders — replace with actual keys
    const val NEWS_API_KEY = ""
    const val GNEWS_API_KEY = ""

    // Edition settings
    const val DEFAULT_READING_TIME_MINUTES = 10
    const val DEFAULT_DELIVERY_HOUR = 7
    const val MAX_ARTICLES_PER_SECTION = 5
    const val ARTICLE_CACHE_HOURS = 48

    // LLM settings
    const val LLM_MODEL_NAME = "phi-3-mini-4k-q4"
    const val LLM_MIN_RAM_MB = 2000
    const val LLM_MIN_BATTERY_PERCENT = 20
}
