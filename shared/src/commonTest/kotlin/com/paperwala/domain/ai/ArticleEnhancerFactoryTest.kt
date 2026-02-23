package com.paperwala.domain.ai

import com.paperwala.domain.model.Article
import com.paperwala.domain.model.TopicCategory
import com.paperwala.domain.model.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ArticleEnhancerFactoryTest {

    private val fakeCloudEnhancer = object : ArticleEnhancer {
        override val status = AiStatus.CLOUD
        override suspend fun enhance(
            articles: List<Article>,
            userTopics: List<TopicCategory>
        ) = articles.map { EnhancedArticle(it, "cloud summary", 0.8f) }
    }

    private val fakeLocalEnhancer = object : ArticleEnhancer {
        override val status = AiStatus.LOCAL_LLM
        override suspend fun enhance(
            articles: List<Article>,
            userTopics: List<TopicCategory>
        ) = articles.map { EnhancedArticle(it, "local summary", 0.9f) }
    }

    private val ruleBasedEnhancer = RuleBasedEnhancer()

    @Test
    fun returnsCloudEnhancerByDefault() {
        val factory = ArticleEnhancerFactory(
            cloudEnhancer = fakeCloudEnhancer,
            ruleBasedEnhancer = ruleBasedEnhancer
        )

        val prefs = UserPreferences(enableLocalLlm = false)
        val enhancer = factory.create(prefs)

        assertEquals(AiStatus.CLOUD, enhancer.status)
    }

    @Test
    fun returnsLocalEnhancerWhenEnabledAndAvailable() {
        val factory = ArticleEnhancerFactory(
            cloudEnhancer = fakeCloudEnhancer,
            ruleBasedEnhancer = ruleBasedEnhancer,
            localEnhancer = fakeLocalEnhancer
        )

        val prefs = UserPreferences(enableLocalLlm = true)
        val enhancer = factory.create(prefs)

        assertEquals(AiStatus.LOCAL_LLM, enhancer.status)
    }

    @Test
    fun fallsBackToCloudWhenLocalEnabledButNotAvailable() {
        val factory = ArticleEnhancerFactory(
            cloudEnhancer = fakeCloudEnhancer,
            ruleBasedEnhancer = ruleBasedEnhancer,
            localEnhancer = null
        )

        val prefs = UserPreferences(enableLocalLlm = true)
        val enhancer = factory.create(prefs)

        assertEquals(AiStatus.CLOUD, enhancer.status)
    }

    @Test
    fun fallbackReturnsRuleBasedEnhancer() {
        val factory = ArticleEnhancerFactory(
            cloudEnhancer = fakeCloudEnhancer,
            ruleBasedEnhancer = ruleBasedEnhancer
        )

        val fallback = factory.fallback()

        assertEquals(AiStatus.RULE_BASED, fallback.status)
        assertIs<RuleBasedEnhancer>(fallback)
    }
}
