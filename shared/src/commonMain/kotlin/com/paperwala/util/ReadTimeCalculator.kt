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

object ReadTimeCalculator {

    private const val WORDS_PER_MINUTE = 200

    fun estimateMinutes(content: String?): Int {
        if (content.isNullOrBlank()) return 3
        val wordCount = content.trim().split(Regex("\\s+")).size
        return maxOf(1, wordCount / WORDS_PER_MINUTE)
    }

    fun estimateEditionMinutes(articleReadTimes: List<Int>): Int {
        return articleReadTimes.sum()
    }

    fun formatReadTime(minutes: Int): String {
        return if (minutes < 1) "< 1 min read" else "$minutes min read"
    }
}
