package com.paperwala.di

import com.paperwala.data.local.db.DatabaseDriverFactory
import com.paperwala.data.local.db.PaperwalaDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        PaperwalaDatabase(driver)
    }
}
