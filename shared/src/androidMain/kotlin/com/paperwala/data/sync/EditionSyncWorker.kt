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
package com.paperwala.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paperwala.data.repository.UserRepository
import com.paperwala.domain.usecase.GenerateMorningEditionUseCase
import com.paperwala.util.NotificationManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditionSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val generateEditionUseCase: GenerateMorningEditionUseCase by inject()
    private val userRepository: UserRepository by inject()
    private val notificationManager: NotificationManager by inject()

    override suspend fun doWork(): Result {
        return try {
            val edition = generateEditionUseCase.execute(forceRefresh = true)
            val prefs = userRepository.getPreferences()
            if (prefs.enableNotifications) {
                notificationManager.showEditionReady(edition.articleCount)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
