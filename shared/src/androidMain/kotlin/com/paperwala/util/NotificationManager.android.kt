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
package com.paperwala.util

import android.Manifest
import android.app.NotificationChannel
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.NotificationManager as AndroidNotificationManager

actual class NotificationManager(private val context: Context) {

    actual fun requestPermission() {
        // Create the notification channel (required for Android 8+).
        // Runtime permission (POST_NOTIFICATIONS) must be requested from an Activity.
        createChannel()
    }

    actual fun showEditionReady(articleCount: Int) {
        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your morning edition is ready!")
            .setContentText("$articleCount stories curated for you.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    actual fun areNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Morning Edition",
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notification when your morning edition is ready"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "paperwala_morning_edition"
        const val NOTIFICATION_ID = 1001
    }
}
