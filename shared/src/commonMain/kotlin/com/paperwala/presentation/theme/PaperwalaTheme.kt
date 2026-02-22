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
package com.paperwala.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PaperwalaColors.MastheadRed,
    onPrimary = PaperwalaColors.PaperWhite,
    primaryContainer = PaperwalaColors.MastheadRed.copy(alpha = 0.12f),
    secondary = PaperwalaColors.SectionBlue,
    onSecondary = PaperwalaColors.PaperWhite,
    background = PaperwalaColors.PaperCream,
    onBackground = PaperwalaColors.InkBlack,
    surface = PaperwalaColors.SurfaceLight,
    onSurface = PaperwalaColors.InkBlack,
    surfaceVariant = PaperwalaColors.CardBackground,
    onSurfaceVariant = PaperwalaColors.InkGray,
    outline = PaperwalaColors.DividerColor,
    error = PaperwalaColors.MastheadRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PaperwalaColors.MastheadRed.copy(alpha = 0.85f),
    onPrimary = PaperwalaColors.PaperWhite,
    secondary = PaperwalaColors.LinkBlue,
    onSecondary = PaperwalaColors.PaperWhite,
    background = PaperwalaColors.SurfaceDark,
    onBackground = PaperwalaColors.PaperCream,
    surface = PaperwalaColors.InkDarkGray,
    onSurface = PaperwalaColors.PaperCream,
    surfaceVariant = PaperwalaColors.InkDarkGray,
    onSurfaceVariant = PaperwalaColors.InkLightGray,
    outline = PaperwalaColors.InkGray,
    error = PaperwalaColors.MastheadRed.copy(alpha = 0.85f)
)

@Composable
fun PaperwalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NewspaperTypography(),
        content = content
    )
}
