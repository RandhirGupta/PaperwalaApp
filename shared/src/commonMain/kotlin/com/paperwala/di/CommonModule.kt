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

import com.paperwala.data.remote.api.GNewsApiService
import com.paperwala.data.remote.api.NewsApiService
import com.paperwala.data.remote.api.NewsdataApiService
import com.paperwala.data.remote.api.RssFeedService
import com.paperwala.data.remote.createHttpClient
import com.paperwala.data.repository.EditionRepository
import com.paperwala.data.repository.NewsRepository
import com.paperwala.data.repository.UserRepository
import com.paperwala.domain.usecase.GenerateMorningEditionUseCase
import com.paperwala.util.Constants
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single { NewsApiService(get()) }
    single { GNewsApiService(get()) }
    single { NewsdataApiService(get()) }
    single { RssFeedService(get()) }
}

val repositoryModule = module {
    single {
        NewsRepository(
            database = get(),
            newsApiService = get(),
            gNewsApiService = get(),
            newsdataApiService = get(),
            rssFeedService = get(),
            newsApiKey = Constants.NEWS_API_KEY,
            gNewsApiKey = Constants.GNEWS_API_KEY,
            newsdataApiKey = Constants.NEWSDATA_API_KEY
        )
    }
    single { UserRepository(database = get()) }
    single { EditionRepository(database = get()) }
}

val useCaseModule = module {
    factory {
        GenerateMorningEditionUseCase(
            newsRepository = get(),
            editionRepository = get(),
            userRepository = get()
        )
    }
}

val commonModules = listOf(networkModule, repositoryModule, useCaseModule)
