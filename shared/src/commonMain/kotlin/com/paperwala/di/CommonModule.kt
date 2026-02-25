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
package com.paperwala.di

import com.paperwala.data.mapper.ArticleMapper
import com.paperwala.data.remote.api.GeminiApiService
import com.paperwala.data.remote.api.GNewsApi
import com.paperwala.data.remote.api.GNewsApiService
import com.paperwala.data.remote.api.NewsApi
import com.paperwala.data.remote.api.NewsApiService
import com.paperwala.data.remote.api.RssFeedApi
import com.paperwala.data.remote.api.RssFeedService
import com.paperwala.data.remote.createHttpClient
import com.paperwala.data.repository.EditionRepositoryImpl
import com.paperwala.data.repository.NewsRepositoryImpl
import com.paperwala.data.repository.ReadingStreakRepositoryImpl
import com.paperwala.data.repository.UserRepositoryImpl
import com.paperwala.domain.repository.EditionRepository
import com.paperwala.domain.repository.NewsRepository
import com.paperwala.domain.repository.ReadingStreakRepository
import com.paperwala.domain.repository.UserRepository
import com.paperwala.domain.ai.ArticleEnhancerFactory
import com.paperwala.domain.ai.CloudArticleEnhancer
import com.paperwala.domain.ai.RuleBasedEnhancer
import com.paperwala.domain.usecase.GenerateMorningEditionUseCase
import com.paperwala.domain.usecase.GetBookmarksUseCase
import com.paperwala.domain.usecase.GetReadingStreakUseCase
import com.paperwala.domain.usecase.MarkArticleReadUseCase
import com.paperwala.domain.usecase.ToggleBookmarkUseCase
import com.paperwala.util.ApiKeys
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single<NewsApi> { NewsApiService(get()) }
    single<GNewsApi> { GNewsApiService(get()) }
    single<RssFeedApi> { RssFeedService(get()) }
    single { GeminiApiService(get()) }
}

val repositoryModule = module {
    single { ArticleMapper() }
    single<NewsRepository> {
        NewsRepositoryImpl(
            database = get(),
            newsApiService = get(),
            gNewsApiService = get(),
            rssFeedService = get(),
            articleMapper = get(),
            newsApiKey = ApiKeys.NEWS_API_KEY,
            gNewsApiKey = ApiKeys.GNEWS_API_KEY
        )
    }
    single<UserRepository> { UserRepositoryImpl(database = get()) }
    single<EditionRepository> { EditionRepositoryImpl(database = get()) }
    single<ReadingStreakRepository> { ReadingStreakRepositoryImpl(database = get()) }
}

val aiModule = module {
    single { RuleBasedEnhancer() }
    single { CloudArticleEnhancer(geminiApiService = get(), apiKey = ApiKeys.GEMINI_API_KEY) }
    single {
        ArticleEnhancerFactory(
            cloudEnhancer = get<CloudArticleEnhancer>(),
            ruleBasedEnhancer = get()
        )
    }
}

val useCaseModule = module {
    single {
        GenerateMorningEditionUseCase(
            newsRepository = get(),
            editionRepository = get(),
            userRepository = get(),
            articleEnhancerFactory = get()
        )
    }
    single { ToggleBookmarkUseCase(newsRepository = get()) }
    single { GetBookmarksUseCase(newsRepository = get()) }
    single { GetReadingStreakUseCase(readingStreakRepository = get()) }
    single { MarkArticleReadUseCase(newsRepository = get()) }
}

val commonModules = listOf(networkModule, repositoryModule, aiModule, useCaseModule)
