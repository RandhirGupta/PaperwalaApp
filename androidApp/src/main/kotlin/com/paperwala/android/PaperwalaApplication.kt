package com.paperwala.android

import android.app.Application
import com.paperwala.di.androidModule
import com.paperwala.di.commonModules
import com.paperwala.di.databaseModule
import com.paperwala.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PaperwalaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@PaperwalaApplication)
            modules(
                commonModules +
                        listOf(
                            databaseModule,
                            androidModule,
                            presentationModule
                        )
            )
        }
    }
}
