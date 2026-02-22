package com.paperwala.di

import org.koin.core.context.startKoin

object KoinInit {
    fun doInit() {
        startKoin {
            modules(
                commonModules +
                        listOf(
                            databaseModule,
                            iosModule,
                            presentationModule
                        )
            )
        }
    }
}
