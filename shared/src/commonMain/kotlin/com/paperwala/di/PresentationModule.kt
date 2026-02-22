package com.paperwala.di

import com.paperwala.presentation.screens.morningedition.MorningEditionViewModel
import com.paperwala.presentation.screens.onboarding.OnboardingViewModel
import org.koin.dsl.module

val presentationModule = module {
    factory { OnboardingViewModel(userRepository = get()) }
    factory {
        MorningEditionViewModel(
            generateEditionUseCase = get(),
            newsRepository = get(),
            userRepository = get()
        )
    }
}
