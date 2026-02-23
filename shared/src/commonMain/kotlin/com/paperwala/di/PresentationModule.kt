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

import com.paperwala.presentation.screens.articledetail.ArticleDetailViewModel
import com.paperwala.presentation.screens.bookmarks.BookmarksViewModel
import com.paperwala.presentation.screens.morningedition.MorningEditionViewModel
import com.paperwala.presentation.screens.onboarding.OnboardingViewModel
import com.paperwala.presentation.screens.settings.SettingsViewModel
import com.paperwala.presentation.screens.streaks.StreakDashboardViewModel
import org.koin.dsl.module

val presentationModule = module {
    factory { OnboardingViewModel(userRepository = get(), syncScheduler = get()) }
    factory {
        MorningEditionViewModel(
            generateEditionUseCase = get(),
            newsRepository = get(),
            userRepository = get(),
            connectivityObserver = get(),
            readingStreakRepository = get()
        )
    }
    factory {
        SettingsViewModel(
            userRepository = get(),
            modelManager = get(),
            notificationManager = get(),
            syncScheduler = get()
        )
    }
    factory { BookmarksViewModel(newsRepository = get()) }
    factory { StreakDashboardViewModel(readingStreakRepository = get()) }
    factory { ArticleDetailViewModel(newsRepository = get(), shareManager = get()) }
}
