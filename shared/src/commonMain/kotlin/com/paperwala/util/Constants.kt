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
