package com.paperwala.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopicCategoryTest {

    @Test
    fun allCategoriesHaveNonEmptyDisplayName() {
        TopicCategory.entries.forEach { category ->
            assertTrue(category.displayName.isNotBlank(), "${category.name} should have a displayName")
        }
    }

    @Test
    fun allCategoriesHaveNonEmptyIcon() {
        TopicCategory.entries.forEach { category ->
            assertTrue(category.icon.isNotBlank(), "${category.name} should have an icon")
        }
    }

    @Test
    fun fromStringReturnsCorrectCategoryForExactMatch() {
        TopicCategory.entries.forEach { category ->
            assertEquals(category, TopicCategory.fromString(category.name))
        }
    }

    @Test
    fun fromStringIsCaseInsensitive() {
        assertEquals(TopicCategory.TECHNOLOGY, TopicCategory.fromString("technology"))
        assertEquals(TopicCategory.SPORTS, TopicCategory.fromString("Sports"))
        assertEquals(TopicCategory.BUSINESS, TopicCategory.fromString("BUSINESS"))
    }

    @Test
    fun fromStringDefaultsToWorldNewsForUnknown() {
        assertEquals(TopicCategory.WORLD_NEWS, TopicCategory.fromString("unknown"))
        assertEquals(TopicCategory.WORLD_NEWS, TopicCategory.fromString(""))
        assertEquals(TopicCategory.WORLD_NEWS, TopicCategory.fromString("nonexistent"))
    }

    @Test
    fun twelveCategoriesExist() {
        assertEquals(12, TopicCategory.entries.size)
    }

    @Test
    fun expectedCategoriesArePresent() {
        val names = TopicCategory.entries.map { it.name }.toSet()
        assertTrue("POLITICS" in names)
        assertTrue("TECHNOLOGY" in names)
        assertTrue("SPORTS" in names)
        assertTrue("BUSINESS" in names)
        assertTrue("ENTERTAINMENT" in names)
        assertTrue("SCIENCE" in names)
        assertTrue("HEALTH" in names)
        assertTrue("WORLD_NEWS" in names)
        assertTrue("INDIA" in names)
        assertTrue("OPINION" in names)
        assertTrue("ENVIRONMENT" in names)
        assertTrue("EDUCATION" in names)
    }
}
