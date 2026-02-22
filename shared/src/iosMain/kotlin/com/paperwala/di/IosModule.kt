package com.paperwala.di

import com.paperwala.data.local.db.DatabaseDriverFactory
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
}
