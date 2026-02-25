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
package com.paperwala

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.setSingletonImageLoaderFactory
import com.paperwala.domain.repository.UserRepository
import com.paperwala.domain.model.ThemeMode
import com.paperwala.presentation.image.createImageLoader
import com.paperwala.presentation.navigation.AppNavigator
import com.paperwala.presentation.theme.LocalFontScale
import com.paperwala.presentation.theme.LocalThemeMode
import com.paperwala.presentation.theme.LocalThemeUpdater
import com.paperwala.presentation.theme.PaperwalaTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val userRepository: UserRepository = koinInject()
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var fontScale by remember { mutableFloatStateOf(1.0f) }

    LaunchedEffect(Unit) {
        val prefs = userRepository.getPreferences()
        themeMode = prefs.themeMode
        fontScale = prefs.fontScale
    }

    setSingletonImageLoaderFactory { context ->
        createImageLoader(context)
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalFontScale provides fontScale,
        LocalThemeUpdater provides { mode, scale ->
            themeMode = mode
            fontScale = scale
        }
    ) {
        PaperwalaTheme(themeMode = themeMode, fontScale = fontScale) {
            AppNavigator()
        }
    }
}
