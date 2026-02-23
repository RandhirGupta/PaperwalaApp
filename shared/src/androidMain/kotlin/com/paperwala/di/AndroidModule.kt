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

import com.paperwala.data.local.db.DatabaseDriverFactory
import com.paperwala.data.sync.BackgroundSyncScheduler
import com.paperwala.domain.ai.LocalLlmEngine
import com.paperwala.domain.ai.ModelManager
import com.paperwala.util.ConnectivityObserver
import com.paperwala.util.NotificationManager
import com.paperwala.util.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { ConnectivityObserver(androidContext()) }
    single { BackgroundSyncScheduler(androidContext()) }
    single { LocalLlmEngine(androidContext()) }
    single { ModelManager(androidContext()) }
    single { ShareManager(androidContext()) }
    single { NotificationManager(androidContext()) }
}
