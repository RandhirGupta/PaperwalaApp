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

import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents

actual class BackgroundSyncScheduler {

    actual fun scheduleEditionSync(deliveryHour: Int) {
        val request = BGAppRefreshTaskRequest(identifier = TASK_IDENTIFIER)

        // Schedule ~1 hour before delivery
        val syncHour = (deliveryHour - 1).coerceAtLeast(0)
        val calendar = NSCalendar.currentCalendar
        val components = NSDateComponents().apply {
            hour = syncHour.toLong()
            minute = 0
        }
        val nextSyncDate = calendar.nextDateAfterDate(
            NSDate(),
            matchingComponents = components,
            options = 0u
        )
        request.earliestBeginDate = nextSyncDate

        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
        } catch (_: Exception) {
            // BG task submission can fail on simulator
        }
    }

    actual fun cancelSync() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(TASK_IDENTIFIER)
    }

    companion object {
        const val TASK_IDENTIFIER = "com.paperwala.edition.refresh"
    }
}
