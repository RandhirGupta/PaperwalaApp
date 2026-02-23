package com.paperwala.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadTimeCalculatorTest {

    @Test
    fun nullContentReturnsDefault() {
        assertEquals(3, ReadTimeCalculator.estimateMinutes(null))
    }

    @Test
    fun blankContentReturnsDefault() {
        assertEquals(3, ReadTimeCalculator.estimateMinutes(""))
        assertEquals(3, ReadTimeCalculator.estimateMinutes("   "))
    }

    @Test
    fun shortContentReturnsMinimumOneMinute() {
        assertEquals(1, ReadTimeCalculator.estimateMinutes("Hello world"))
    }

    @Test
    fun twoHundredWordsEqualsOneMinute() {
        val words = (1..200).joinToString(" ") { "word" }
        assertEquals(1, ReadTimeCalculator.estimateMinutes(words))
    }

    @Test
    fun fourHundredWordsEqualsTwoMinutes() {
        val words = (1..400).joinToString(" ") { "word" }
        assertEquals(2, ReadTimeCalculator.estimateMinutes(words))
    }

    @Test
    fun oneThousandWordsEqualsFiveMinutes() {
        val words = (1..1000).joinToString(" ") { "word" }
        assertEquals(5, ReadTimeCalculator.estimateMinutes(words))
    }

    @Test
    fun estimateEditionMinutesSumsArticleTimes() {
        assertEquals(0, ReadTimeCalculator.estimateEditionMinutes(emptyList()))
        assertEquals(10, ReadTimeCalculator.estimateEditionMinutes(listOf(3, 3, 4)))
        assertEquals(15, ReadTimeCalculator.estimateEditionMinutes(listOf(5, 5, 5)))
    }

    @Test
    fun formatReadTimeShowsMinutes() {
        assertEquals("3 min read", ReadTimeCalculator.formatReadTime(3))
        assertEquals("10 min read", ReadTimeCalculator.formatReadTime(10))
        assertEquals("1 min read", ReadTimeCalculator.formatReadTime(1))
    }

    @Test
    fun formatReadTimeLessThanOneMinute() {
        assertEquals("< 1 min read", ReadTimeCalculator.formatReadTime(0))
    }
}
