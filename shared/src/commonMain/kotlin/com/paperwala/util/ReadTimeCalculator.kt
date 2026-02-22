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
