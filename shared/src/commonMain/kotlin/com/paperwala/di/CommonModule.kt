package com.paperwala.di

import com.paperwala.data.remote.api.GNewsApiService
import com.paperwala.data.remote.api.NewsApiService
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
    single { RssFeedService(get()) }
}

val repositoryModule = module {
    single {
        NewsRepository(
            database = get(),
            newsApiService = get(),
            gNewsApiService = get(),
            rssFeedService = get(),
            newsApiKey = Constants.NEWS_API_KEY,
            gNewsApiKey = Constants.GNEWS_API_KEY
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
